package com.cookiesstore.admin.service.products.validation;

import com.cookiesstore.admin.service.products.PackageOptionValidationException;
import com.cookiesstore.admin.service.products.PackageProductService;
import com.cookiesstore.admin.web.dto.products.CreateProductForm;
import com.cookiesstore.admin.web.dto.products.PackageComponentForm;
import com.cookiesstore.admin.web.dto.products.PackageComponentItemForm;
import com.cookiesstore.admin.web.dto.products.UpdateProductForm;
import com.cookiesstore.common.entities.Product;
import com.cookiesstore.common.repositories.PackageOptionTypeRepository;
import com.cookiesstore.common.repositories.ProductRepository;
import com.cookiesstore.common.repositories.SourceRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class PackageProductValidationStrategy implements ProductTypeValidationStrategy {

    private final ProductRepository productRepository;
    private final SourceRepository sourceRepository;
    private final PackageOptionTypeRepository packageOptionTypeRepository;

    public PackageProductValidationStrategy(
        ProductRepository productRepository,
        SourceRepository sourceRepository,
        PackageOptionTypeRepository packageOptionTypeRepository
    ) {
        this.productRepository = productRepository;
        this.sourceRepository = sourceRepository;
        this.packageOptionTypeRepository = packageOptionTypeRepository;
    }

    @Override
    public String supportedTypeCode() {
        return PackageProductService.TYPE_CODE;
    }

    @Override
    public void validateCreate(CreateProductForm form) {
        validatePackageOptions(form.packageOptions());
    }

    @Override
    public void validateUpdate(Long productId, UpdateProductForm form) {
        validatePackageOptions(form.packageOptions());
    }

    private void validatePackageOptions(List<PackageComponentForm> packageOptions) {
        if (packageOptions == null || packageOptions.isEmpty()) {
            throw new PackageOptionValidationException("admin.products.package.options.required");
        }

        Set<Long> optionTypeIds = new HashSet<>();
        Set<String> optionNames = new HashSet<>();
        for (PackageComponentForm option : packageOptions) {
            if (option == null) {
                throw new PackageOptionValidationException("admin.products.package.options.null-entry");
            }

            if (option.optionTypeId() == null) {
                throw new PackageOptionValidationException("admin.products.package.options.type-required");
            }
            optionTypeIds.add(option.optionTypeId());

            if (option.name() == null || option.name().isBlank()) {
                throw new PackageOptionValidationException("admin.products.package.options.name-required");
            }

            String normalizedName = option.name().trim().toLowerCase(Locale.ROOT);
            if (!optionNames.add(normalizedName)) {
                throw new PackageOptionValidationException("admin.products.package.options.name-duplicate");
            }

            if (option.sortOrder() < 0) {
                throw new PackageOptionValidationException("admin.products.package.options.sort-invalid");
            }

            validateSelectionBounds(option);

            if (option.items() == null || option.items().isEmpty()) {
                throw new PackageOptionValidationException("admin.products.package.options.items-required");
            }

            validateOptionItems(option);
            validateReferencedProducts(option);
            validateReferencedSources(option);
        }

        long foundTypes = packageOptionTypeRepository.findAllById(optionTypeIds).size();
        if (foundTypes != optionTypeIds.size()) {
            throw new PackageOptionValidationException("admin.products.package.options.type-invalid");
        }
    }

    private void validateSelectionBounds(PackageComponentForm option) {
        Integer minSelect = option.minSelect();
        Integer maxSelect = option.maxSelect();

        if (minSelect != null && minSelect < 0) {
            throw new PackageOptionValidationException("admin.products.package.options.min-invalid");
        }
        if (maxSelect != null && maxSelect < 0) {
            throw new PackageOptionValidationException("admin.products.package.options.max-invalid");
        }
        if (minSelect != null && maxSelect != null && maxSelect < minSelect) {
            throw new PackageOptionValidationException("admin.products.package.options.range-invalid");
        }
    }

    private void validateOptionItems(PackageComponentForm option) {
        Set<String> itemKeys = new HashSet<>();
        int defaultCount = 0;
        int itemCount = option.items().size();

        for (PackageComponentItemForm item : option.items()) {
            if (item == null) {
                throw new PackageOptionValidationException("admin.products.package.items.null-entry");
            }
            if (item.productId() == null) {
                throw new PackageOptionValidationException("admin.products.package.items.product-required");
            }
            if (item.sortOrder() < 0) {
                throw new PackageOptionValidationException("admin.products.package.items.sort-invalid");
            }

            String uniqueKey = item.productId() + "|" + item.sourceId();
            if (!itemKeys.add(uniqueKey)) {
                throw new PackageOptionValidationException("admin.products.package.items.duplicate");
            }

            if (item.isDefault()) {
                defaultCount++;
            }

            validateItemExtraPrice(item);
        }

        Integer minSelect = option.minSelect();
        Integer maxSelect = option.maxSelect();
        if (minSelect != null && minSelect > itemCount) {
            throw new PackageOptionValidationException("admin.products.package.options.min-over-items");
        }
        if (maxSelect != null && maxSelect > itemCount) {
            throw new PackageOptionValidationException("admin.products.package.options.max-over-items");
        }
        if (maxSelect != null && defaultCount > maxSelect) {
            throw new PackageOptionValidationException("admin.products.package.items.default-over-max");
        }
    }

    private void validateItemExtraPrice(PackageComponentItemForm item) {
        if (item.extraPriceMode() == null || item.extraPriceMode().isBlank()) {
            throw new PackageOptionValidationException("admin.products.package.items.extra-price-mode-required");
        }

        String normalizedMode = item.extraPriceMode().trim().toUpperCase(Locale.ROOT);
        switch (normalizedMode) {
            case "NO_EXTRA" -> {
                if (item.extraPrice() != null) {
                    throw new PackageOptionValidationException("admin.products.package.items.extra-price-must-be-null");
                }
            }
            case "FIXED_EXTRA" -> {
                if (item.extraPrice() == null || item.extraPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
                    throw new PackageOptionValidationException("admin.products.package.items.extra-price-invalid");
                }
            }
            default -> throw new PackageOptionValidationException("admin.products.package.items.extra-price-mode-invalid");
        }
    }

    private void validateReferencedProducts(PackageComponentForm option) {
        Set<Long> productIds = option.items().stream()
            .map(PackageComponentItemForm::productId)
            .collect(Collectors.toSet());

        Map<Long, Product> productsById = productRepository.findAllById(productIds)
            .stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));

        if (productsById.size() != productIds.size()) {
            throw new PackageOptionValidationException("admin.products.package.items.product-invalid");
        }

        if (option.categoryId() == null) {
            return;
        }

        boolean hasInvalidCategory = productsById.values().stream()
            .anyMatch(product -> product.getCategory() == null || !option.categoryId().equals(product.getCategory().getId()));
        if (hasInvalidCategory) {
            throw new PackageOptionValidationException("admin.products.package.items.category-mismatch");
        }
    }

    private void validateReferencedSources(PackageComponentForm option) {
        Set<Long> sourceIds = option.items().stream()
            .map(PackageComponentItemForm::sourceId)
            .filter(id -> id != null)
            .collect(Collectors.toSet());

        if (sourceIds.isEmpty()) {
            return;
        }

        long found = sourceRepository.findAllById(sourceIds).size();
        if (found != sourceIds.size()) {
            throw new PackageOptionValidationException("admin.products.package.items.source-invalid");
        }
    }
}
