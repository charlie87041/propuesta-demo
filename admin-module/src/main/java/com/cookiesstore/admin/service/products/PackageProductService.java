package com.cookiesstore.admin.service.products;

import com.cookiesstore.admin.service.support.AuthenticatedUserProvider;
import com.cookiesstore.admin.web.dto.products.CreateProductForm;
import com.cookiesstore.admin.web.dto.products.PackageComponentForm;
import com.cookiesstore.admin.web.dto.products.PackageComponentItemForm;
import com.cookiesstore.admin.web.dto.products.UpdateProductForm;
import com.cookiesstore.common.entities.PackageOptionItemExtraPriceMode;
import com.cookiesstore.common.entities.PackageOption;
import com.cookiesstore.common.entities.PackageOptionItem;
import com.cookiesstore.common.entities.Product;
import com.cookiesstore.common.repositories.CategoryRepository;
import com.cookiesstore.common.repositories.PackageOptionItemRepository;
import com.cookiesstore.common.repositories.PackageOptionRepository;
import com.cookiesstore.common.repositories.PackageOptionTypeRepository;
import com.cookiesstore.common.repositories.ProductRepository;
import com.cookiesstore.common.repositories.SourceRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PackageProductService extends AbstractCompositeProductService {

    public static final String TYPE_CODE = "PACKAGE";

    private final PackageOptionRepository packageOptionRepository;
    private final PackageOptionItemRepository packageOptionItemRepository;
    private final PackageOptionTypeRepository packageOptionTypeRepository;
    private final SourceRepository sourceRepository;

    public PackageProductService(
        ProductRepository productRepository,
        CategoryRepository categoryRepository,
        SourceRepository sourceRepository,
        PackageOptionRepository packageOptionRepository,
        PackageOptionItemRepository packageOptionItemRepository,
        PackageOptionTypeRepository packageOptionTypeRepository,
        ProductPriceService productPriceService,
        ProductSourceService productSourceService,
        ProductTemplateService productTemplateService,
        AuthenticatedUserProvider authenticatedUserProvider
    ) {
        super(
            productRepository,
            categoryRepository,
            productPriceService,
            productSourceService,
            productTemplateService,
            authenticatedUserProvider
        );
        this.sourceRepository = sourceRepository;
        this.packageOptionRepository = packageOptionRepository;
        this.packageOptionItemRepository = packageOptionItemRepository;
        this.packageOptionTypeRepository = packageOptionTypeRepository;
    }

    @Override
    public String productTypeCode() {
        return TYPE_CODE;
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProduct(Long productId) {
        return productRepository
            .findPackageByIdAndProductTypeCode(productId, TYPE_CODE)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    }


    @Override
    @Transactional(readOnly = true)
    public UpdateProductForm buildUpdateProductForm(Long productId) {
        UpdateProductForm baseForm = super.buildUpdateProductForm(productId);
        List<PackageComponentForm> packageOptions = packageOptionRepository.findByPackageProductIdOrderBySortOrderAsc(productId)
            .stream()
            .map((PackageOption packageOption) -> new PackageComponentForm(
                packageOption.getPackageProduct().getId(),
                packageOption.getOptionType().getId(),
                packageOption.getCategory() != null ? packageOption.getCategory().getId() : null,
                packageOption.getName(),
                packageOption.getMinSelect(),
                packageOption.getMaxSelect(),
                (packageOption.getItems() == null ? java.util.stream.Stream.<PackageOptionItem>empty() : packageOption.getItems().stream())
                    .sorted(java.util.Comparator.comparingInt(PackageOptionItem::getSortOrder))
                    .map((PackageOptionItem item) -> new PackageComponentItemForm(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getSource() != null ? item.getSource().getId() : null,
                        item.getSortOrder(),
                        item.isDefault(),
                        item.getExtraPriceMode().name(),
                        item.getExtraPrice()
                    ))
                    .toList(),
                packageOption.getSortOrder()    
                ))
            .toList();
        return mergeCompositionIntoUpdateForm(
            baseForm,
            TYPE_CODE,
            List.of(),
            packageOptions
        );

    }

    @Override
    public Product createProduct(CreateProductForm form) {
        List<PackageComponentForm> packageOptions = form.packageOptions();
        BigDecimal resolvedPrice = calculateResolvedPackagePrice(form.price(), packageOptions);
        CreateProductForm packageForm = buildCompositeCreateForm(
            form,
            resolvedPrice,
            TYPE_CODE,
            List.of(),
            packageOptions,
            true,
            true,
            true
        );
        Product product = super.createProduct(packageForm);
        persistPackageOptions(product, packageOptions);
        return getProduct(product.getId());
    }

    @Override
    public Product updateProduct(Long productId, UpdateProductForm form) {
        List<PackageComponentForm> packageOptions = form.packageOptions();
        BigDecimal resolvedPrice = calculateResolvedPackagePrice(form.price(), packageOptions);
        UpdateProductForm packageForm = buildCompositeUpdateForm(
            form,
            resolvedPrice,
            TYPE_CODE,
            List.of(),
            packageOptions
        );
        Product product = super.updateProduct(productId, packageForm);
        replacePackageOptions(product, packageOptions);
        return getProduct(product.getId());
    }

    private void replacePackageOptions(Product product, List<PackageComponentForm> packageOptions) {
        List<PackageOption> existing = packageOptionRepository.findByPackageProductIdOrderBySortOrderAsc(product.getId());
        if (!existing.isEmpty()) {
            packageOptionRepository.deleteAll(existing);
            packageOptionRepository.flush();
        }
        persistPackageOptions(product, packageOptions);
    }

    private void persistPackageOptions(Product product, List<PackageComponentForm> packageOptions) {
        if (packageOptions == null || packageOptions.isEmpty()) {
            return;
        }

        for (PackageComponentForm optionForm : packageOptions) {
            PackageOption option = new PackageOption();
            option.setPackageProduct(product);
            option.setOptionType(packageOptionTypeRepository.getReferenceById(optionForm.optionTypeId()));
            option.setCategory(optionForm.categoryId() == null ? null : categoryRepository.getReferenceById(optionForm.categoryId()));
            option.setName(optionForm.name() == null ? null : optionForm.name().trim());
            option.setMinSelect(optionForm.minSelect());
            option.setMaxSelect(optionForm.maxSelect());
            option.setRequired(optionForm.minSelect() != null && optionForm.minSelect() > 0);
            option.setSortOrder(optionForm.sortOrder());
            PackageOption savedOption = packageOptionRepository.save(option);

            List<PackageComponentItemForm> items = optionForm.items() == null ? List.of() : optionForm.items();
            for (PackageComponentItemForm itemForm : items) {
                PackageOptionItem item = new PackageOptionItem();
                item.setPackageOption(savedOption);
                item.setProduct(productRepository.getReferenceById(itemForm.productId()));
                item.setSource(itemForm.sourceId() == null ? null : sourceRepository.getReferenceById(itemForm.sourceId()));
                item.setDefault(itemForm.isDefault());
                item.setSortOrder(itemForm.sortOrder());
                PackageOptionItemExtraPriceMode mode = PackageOptionItemExtraPriceMode.valueOf(itemForm.extraPriceMode().trim().toUpperCase());
                item.setExtraPriceMode(mode);
                item.setExtraPrice(mode == PackageOptionItemExtraPriceMode.FIXED_EXTRA && itemForm.extraPrice() != null
                    ? itemForm.extraPrice()
                    : null
                );
                packageOptionItemRepository.save(item);
            }
        }
    }

    private BigDecimal calculateResolvedPackagePrice(BigDecimal basePrice, List<PackageComponentForm> packageOptions) {
        if (packageOptions == null || packageOptions.isEmpty()) {
            return basePrice;
        }

        Set<Long> typeIds = packageOptions.stream()
            .map(PackageComponentForm::optionTypeId)
            .collect(Collectors.toSet());
        Map<Long, String> optionTypeCodes = packageOptionTypeRepository.findAllById(typeIds).stream()
            .collect(Collectors.toMap(type -> type.getId(), type -> type.getCode()));

        BigDecimal totalExtras = BigDecimal.ZERO;
        for (PackageComponentForm option : packageOptions) {
            String optionTypeCode = optionTypeCodes.get(option.optionTypeId());
            if (optionTypeCode == null) {
                throw new PackageOptionValidationException("admin.products.package.options.type-invalid");
            }
            totalExtras = totalExtras.add(resolveRequiredExtras(option, optionTypeCode));
        }
        return basePrice.add(totalExtras);
    }

    private BigDecimal resolveRequiredExtras(PackageComponentForm option, String optionTypeCode) {
        List<PackageComponentItemForm> items = option.items() == null ? List.of() : option.items();
        if (items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<BigDecimal> extras = items.stream()
            .map(this::resolveItemExtra)
            .sorted()
            .toList();

        return switch (optionTypeCode) {
            case "ALL_OF" -> extras.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            case "ONE_OF" -> {
                int requiredCount = option.minSelect() != null ? Math.max(0, option.minSelect()) : 1;
                yield sumLowest(extras, requiredCount);
            }
            case "N_OF" -> {
                int requiredCount = option.minSelect() != null ? Math.max(0, option.minSelect()) : 0;
                yield sumLowest(extras, requiredCount);
            }
            default -> throw new PackageOptionValidationException("admin.products.package.options.type-invalid");
        };
    }

    private BigDecimal sumLowest(List<BigDecimal> extras, int count) {
        if (count <= 0 || extras.isEmpty()) {
            return BigDecimal.ZERO;
        }
        int limit = Math.min(count, extras.size());
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < limit; i++) {
            total = total.add(extras.get(i));
        }
        return total;
    }

    private BigDecimal resolveItemExtra(PackageComponentItemForm item) {
        String mode = item.extraPriceMode() == null ? "" : item.extraPriceMode().trim().toUpperCase();
        if ("FIXED_EXTRA".equals(mode) && item.extraPrice() != null) {
            return item.extraPrice();
        }
        return BigDecimal.ZERO;
    }

}
