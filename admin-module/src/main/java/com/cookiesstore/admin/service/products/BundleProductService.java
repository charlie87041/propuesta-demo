package com.cookiesstore.admin.service.products;

import com.cookiesstore.admin.config.PricingProperties;
import com.cookiesstore.admin.service.support.AuthenticatedUserProvider;
import com.cookiesstore.admin.web.dto.products.BundleComponentForm;
import com.cookiesstore.admin.web.dto.products.CreateProductForm;
import com.cookiesstore.admin.web.dto.products.UpdateProductForm;
import com.cookiesstore.common.entities.Price;
import com.cookiesstore.common.entities.ProductComponent;
import com.cookiesstore.common.entities.ProductComponentPriceMode;
import com.cookiesstore.common.entities.Product;
import com.cookiesstore.common.entities.Source;
import com.cookiesstore.common.repositories.CategoryRepository;
import com.cookiesstore.common.repositories.PriceRepository;
import com.cookiesstore.common.repositories.ProductComponentRepository;
import com.cookiesstore.common.repositories.ProductRepository;
import com.cookiesstore.common.repositories.ProductSourceRepository;
import com.cookiesstore.common.repositories.ProductTemplateFieldRepository;
import com.cookiesstore.common.repositories.ProductTemplateFieldValueRepository;
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
public class BundleProductService extends AbstractCompositeProductService {

    public static final String TYPE_CODE = "BUNDLE";

    private final ProductRepository productRepository;
    private final ProductComponentRepository productComponentRepository;
    private final SourceRepository sourceRepository;

    public BundleProductService(
        ProductRepository productRepository,
        CategoryRepository categoryRepository,
        SourceRepository sourceRepository,
        PriceRepository priceRepository,
        ProductComponentRepository productComponentRepository,
        ProductSourceRepository productSourceRepository,
        ProductTemplateFieldRepository productTemplateFieldRepository,
        ProductTemplateFieldValueRepository productTemplateFieldValueRepository,
        PricingProperties pricingProperties,
        AuthenticatedUserProvider authenticatedUserProvider
    ) {
        super(
            productRepository,
            categoryRepository,
            sourceRepository,
            priceRepository,
            productSourceRepository,
            productTemplateFieldRepository,
            productTemplateFieldValueRepository,
            pricingProperties,
            authenticatedUserProvider
        );
        this.productRepository = productRepository;
        this.productComponentRepository = productComponentRepository;
        this.sourceRepository = sourceRepository;
    }

    @Override
    public String productTypeCode() {
        return TYPE_CODE;
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProduct(Long productId) {
        return productRepository
            .findBundleByIdAndProductTypeCode(productId, TYPE_CODE)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    @Override
    @Transactional(readOnly = true)
    public UpdateProductForm buildUpdateProductForm(Long productId) {
        UpdateProductForm baseForm = super.buildUpdateProductForm(productId);
        List<BundleComponentForm> components = productComponentRepository
            .findByParentProductIdOrderBySortOrderAsc(productId)
            .stream()
            .map(component -> new BundleComponentForm(
                component.getChildProduct().getId(),
                component.getSource() == null ? null : component.getSource().getId(),
                component.getQuantity(),
                component.getUnitPriceMode(),
                component.getUnitPriceOverride()
            ))
            .toList();

        return mergeCompositionIntoUpdateForm(
            baseForm,
            TYPE_CODE,
            components,
            List.of()
        );
    }

    @Override
    public Product createProduct(CreateProductForm form) {
        List<BundleComponentForm> components = form.bundleComponents();
        Map<Long, Product> childProducts = resolveChildProducts(components);
        Map<Long, Source> sourcesById = resolveSources(components);
        BigDecimal bundlePrice = calculateBundlePrice(components, childProducts);

        CreateProductForm bundleForm = buildCompositeCreateForm(
            form,
            bundlePrice.doubleValue(),
            TYPE_CODE,
            components,
            List.of(),
            true,
            true,
            true
        );

        Product bundle = super.createProduct(bundleForm);
        validateNoSelfReference(bundle.getId(), components);
        persistComponents(bundle, components, childProducts, sourcesById);
        return getProduct(bundle.getId());
    }

    @Override
    public Product updateProduct(Long productId, UpdateProductForm form) {
        List<BundleComponentForm> components = form.bundleComponents();

        Map<Long, Product> childProducts = resolveChildProducts(components);
        Map<Long, Source> sourcesById = resolveSources(components);
        BigDecimal bundlePrice = calculateBundlePrice(components, childProducts);

        UpdateProductForm bundleForm = buildCompositeUpdateForm(
            form,
            bundlePrice.doubleValue(),
            TYPE_CODE,
            components,
            List.of()
        );

        Product bundle = super.updateProduct(productId, bundleForm);
        productComponentRepository.deleteByParentProductId(bundle.getId());
        persistComponents(bundle, components, childProducts, sourcesById);
        return getProduct(bundle.getId());
    }

    @Override
    public void deleteProduct(Long productId) {
        Product bundle = getProduct(productId);
        productComponentRepository.deleteByParentProductId(bundle.getId());
        super.deleteProduct(bundle.getId());
    }

    @Override
    public void enableProduct(Long productId) {
        Product bundle = getProduct(productId);
        boolean hasComponents = !productComponentRepository
            .findByParentProductIdOrderBySortOrderAsc(bundle.getId())
            .isEmpty();
        if (!hasComponents) {
            throw new BundleComponentsRequiredException();
        }
        super.enableProduct(bundle.getId());
    }

    @Override
    public void deactivateProduct(Long productId) {
        Product bundle = getProduct(productId);
        super.deactivateProduct(bundle.getId());
    }

    private void validateNoSelfReference(Long bundleId, List<BundleComponentForm> components) {
        boolean hasSelfReference = components.stream()
            .anyMatch(component -> component.childProductId() != null && component.childProductId().equals(bundleId));
        if (hasSelfReference) {
            throw new BundleComponentInvalidException("admin.products.bundle.components.self-reference");
        }
    }

    private Map<Long, Product> resolveChildProducts(List<BundleComponentForm> components) {
        Set<Long> childIds = components.stream()
            .map(BundleComponentForm::childProductId)
            .collect(Collectors.toSet());

        Map<Long, Product> productsById = productRepository.findAllById(childIds)
            .stream()
            .collect(Collectors.toMap(Product::getId, product -> product));

        if (productsById.size() != childIds.size()) {
            throw new BundleComponentInvalidException("admin.products.bundle.components.invalid-product");
        }

        return productsById;
    }

    private Map<Long, Source> resolveSources(List<BundleComponentForm> components) {
        Set<Long> sourceIds = components.stream()
            .map(BundleComponentForm::sourceId)
            .filter(id -> id != null)
            .collect(Collectors.toSet());

        if (sourceIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Source> sourcesById = sourceRepository.findAllById(sourceIds)
            .stream()
            .collect(Collectors.toMap(Source::getId, source -> source));

        if (sourcesById.size() != sourceIds.size()) {
            throw new BundleComponentInvalidException("admin.products.bundle.components.invalid-source");
        }

        return sourcesById;
    }

    private BigDecimal calculateBundlePrice(
        List<BundleComponentForm> components,
        Map<Long, Product> childProducts
    ) {
        BigDecimal total = BigDecimal.ZERO;

        for (BundleComponentForm component : components) {
            Product child = childProducts.get(component.childProductId());
            if (child == null) {
                throw new BundleComponentInvalidException("admin.products.bundle.components.invalid-product");
            }

            BigDecimal unitPrice = resolveUnitPrice(component, child);
            total = total.add(unitPrice.multiply(component.quantity()));
        }

        return total;
    }

    private BigDecimal resolveUnitPrice(BundleComponentForm component, Product childProduct) {
        if (component.unitPriceMode() == ProductComponentPriceMode.FIXED_OVERRIDE) {
            if (component.unitPriceOverride() == null) {
                throw new BundleComponentInvalidException("admin.products.bundle.components.override-required");
            }
            return component.unitPriceOverride();
        }

        if (component.unitPriceMode() != ProductComponentPriceMode.INHERIT_PRODUCT_PRICE) {
            throw new BundleComponentInvalidException("admin.products.bundle.components.invalid-price-mode");
        }

        Price currentPrice = childProduct.getCurrentPrice();
        if (currentPrice == null || currentPrice.getAmount() == null) {
            throw new BundleComponentInvalidException("admin.products.bundle.components.missing-child-price");
        }
        return currentPrice.getAmount();
    }

    private void persistComponents(
        Product bundle,
        List<BundleComponentForm> components,
        Map<Long, Product> childProducts,
        Map<Long, Source> sourcesById
    ) {
        int sortOrder = 0;
        for (BundleComponentForm component : components) {
            Product child = childProducts.get(component.childProductId());
            if (child == null) {
                throw new BundleComponentInvalidException("admin.products.bundle.components.invalid-product");
            }

            ProductComponent persisted = new ProductComponent();
            persisted.setParentProduct(bundle);
            persisted.setChildProduct(child);
            persisted.setSource(component.sourceId() == null ? null : sourcesById.get(component.sourceId()));
            persisted.setQuantity(component.quantity());
            persisted.setUnitPriceMode(component.unitPriceMode());
            persisted.setUnitPriceOverride(component.unitPriceMode() == ProductComponentPriceMode.FIXED_OVERRIDE
                ? component.unitPriceOverride()
                : null
            );
            persisted.setSortOrder(sortOrder++);
            productComponentRepository.save(persisted);
        }
    }
}
