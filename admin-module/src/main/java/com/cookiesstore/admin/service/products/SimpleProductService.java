package com.cookiesstore.admin.service.products;

import com.cookiesstore.admin.service.support.AuthenticatedUserProvider;
import com.cookiesstore.admin.web.dto.products.CreateProductForm;
import com.cookiesstore.admin.web.dto.products.UpdateProductForm;
import com.cookiesstore.common.entities.Currency;
import com.cookiesstore.common.entities.Product;
import com.cookiesstore.common.entities.ProductTemplate;
import com.cookiesstore.common.repositories.CategoryRepository;
import com.cookiesstore.common.repositories.ProductRepository;
import com.cookiesstore.common.services.products.ProductTypeService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class SimpleProductService implements ProductTypeService<CreateProductForm, UpdateProductForm> {

    public static final String TYPE_CODE = "SIMPLE";

    protected final ProductRepository productRepository;
    protected final CategoryRepository categoryRepository;
    protected final ProductPriceService productPriceService;
    protected final ProductSourceService productSourceService;
    protected final ProductTemplateService productTemplateService;
    protected final AuthenticatedUserProvider authenticatedUserProvider;

    public SimpleProductService(
        ProductRepository productRepository,
        CategoryRepository categoryRepository,
        ProductPriceService productPriceService,
        ProductSourceService productSourceService,
        ProductTemplateService productTemplateService,
        AuthenticatedUserProvider authenticatedUserProvider
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productPriceService = productPriceService;
        this.productSourceService = productSourceService;
        this.productTemplateService = productTemplateService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Override
    public String productTypeCode() {
        return TYPE_CODE;
    }

    @Transactional(readOnly = true)
    public Product getProduct(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Product createProduct(CreateProductForm form) {
        ProductCreateContext createContext = buildCreateContext(form);
        Product product = createContext.product();
        List<com.cookiesstore.common.entities.Source> sources = createContext.sources();

        try {
            productRepository.save(product);
            Currency defaultCurrency = productPriceService.resolveDefaultCurrency();
            productPriceService.upsertProductCurrentPrice(
                product,
                form.price(),
                defaultCurrency,
                authenticatedUserProvider.currentUserId()
            );
            Map<Long, BigDecimal> resolvedSourcePrices = productSourceService.resolveSourcePricesForSelectedSources(
                sources,
                form.sourcePrices()
            );
            productSourceService.upsertProductSources(
                product,
                sources,
                resolvedSourcePrices,
                form.sourceStockQuantities(),
                form.sourceLowStockThresholds(),
                form.stockQuantity(),
                form.lowStockThreshold(),
                defaultCurrency,
                authenticatedUserProvider.currentUserId(),
                false
            );
            Product persisted = productRepository.save(product);
            productTemplateService.syncTemplateFieldValues(persisted, form.templateValues());
            return persisted;
        } catch (DataIntegrityViolationException ex) {
            throw mapDataIntegrityViolation(ex);
        }
    }

    @Transactional(readOnly = true)
    public UpdateProductForm buildUpdateProductForm(Long productId) {
        Product product = getProduct(productId);
        ProductSourceService.ProductSourceSnapshot sourceSnapshot = productSourceService.buildUpdateSourceSnapshot(productId);

        BigDecimal currentPrice = product.getCurrentPrice() != null && product.getCurrentPrice().getAmount() != null
            ? product.getCurrentPrice().getAmount()
            : BigDecimal.ZERO;
        Map<String, String> templateValues = productTemplateService.buildTemplateValuesMap(productId);

        return new UpdateProductForm(
            product.getSku(),
            product.getName(),
            product.getSlug(),
            product.getDescription(),
            product.getCategory().getId(),
            product.getMainImageUrl(),
            sourceSnapshot.sourceIds(),
            sourceSnapshot.defaultStockQuantity(),
            sourceSnapshot.defaultLowStockThreshold(),
            currentPrice,
            product.getProductTypeCode(),
            List.of(),
            List.of(),
            sourceSnapshot.sourcePrices(),
            sourceSnapshot.sourceStockQuantities(),
            sourceSnapshot.sourceLowStockThresholds(),
            templateValues,
            product.isActive(),
            product.isVisible(),
            product.getListable() == null || product.getListable(),
            product.getSearchable() == null || product.getSearchable(),
            product.getPurchasableAlone() == null || product.getPurchasableAlone(),
            null,
            List.of()
        );
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Product updateProduct(Long productId, UpdateProductForm form) {
        ProductUpdateContext updateContext = buildUpdateContext(productId, form);
        Product product = updateContext.product();
        List<com.cookiesstore.common.entities.Source> sources = updateContext.sources();

        try {
            Currency defaultCurrency = productPriceService.resolveDefaultCurrency();
            productPriceService.upsertProductCurrentPrice(
                product,
                form.price(),
                defaultCurrency,
                authenticatedUserProvider.currentUserId()
            );
            Map<Long, BigDecimal> resolvedSourcePrices = productSourceService.resolveSourcePricesForSelectedSources(
                sources,
                form.sourcePrices()
            );
            productSourceService.upsertProductSources(
                product,
                sources,
                resolvedSourcePrices,
                form.sourceStockQuantities(),
                form.sourceLowStockThresholds(),
                form.stockQuantity(),
                form.lowStockThreshold(),
                defaultCurrency,
                authenticatedUserProvider.currentUserId(),
                true
            );
            Product persisted = productRepository.save(product);
            productTemplateService.syncTemplateFieldValues(persisted, form.templateValues());
            return persisted;
        } catch (DataIntegrityViolationException ex) {
            throw mapDataIntegrityViolation(ex);
        }
    }

    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
        productRepository.delete(product);
    }

    public void enableProduct(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
        product.setActive(true);
        productRepository.save(product);
    }

    public void deactivateProduct(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
        product.setActive(false);
        productRepository.save(product);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
    

    private RuntimeException mapDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null
            ? ex.getMostSpecificCause().getMessage()
            : ex.getMessage();
        if (message != null) {
            String lower = message.toLowerCase();
            if (lower.contains("idx_products_sku")
                || lower.contains("idx_products_slug")
                || lower.contains("products_sku_key")
                || lower.contains("products_slug_key")) {
                return new ProductUniqueConstraintException();
            }
        }
        return ex;
    }

    private ProductCreateContext buildCreateContext(CreateProductForm form) {
        String sku = form.sku().trim();
        String slug = form.slug().trim();

        ensureSkuAndSlugAreUnique(sku, slug);

        List<com.cookiesstore.common.entities.Source> sources = productSourceService.resolveSources(form.sourceIds());
        var resolvedCategory = categoryRepository.findById(form.categoryId())
            .orElseThrow(() -> new ProductCategoryNotFoundException(form.categoryId()));
        ProductTemplate categoryTemplate = resolvedCategory.getDefaultTemplate();

        Product product = new Product();
        product.setProductTypeCode(form.productTypeCode());
        product.setSku(sku);
        product.setName(form.name().trim());
        product.setSlug(slug);
        product.setDescription(trimToNull(form.description()));
        product.setCategory(resolvedCategory);
        product.setMainImageUrl(trimToNull(form.mainImageUrl()));
        product.setActive(form.active());
        product.setVisible(form.visible());
        product.setListable(form.isListable());
        product.setSearchable(form.isPurchasable());
        product.setPurchasableAlone(form.isPurchasableAlone());
        product.setTemplate(categoryTemplate);

        return new ProductCreateContext(product, sources);
    }

    private ProductUpdateContext buildUpdateContext(Long productId, UpdateProductForm form) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

        String sku = form.sku().trim();
        String slug = form.slug().trim();
        ensureSkuAndSlugAreUniqueForUpdate(productId, sku, slug);

        List<com.cookiesstore.common.entities.Source> sources = productSourceService.resolveSources(form.sourceIds());
        var resolvedCategory = categoryRepository.findById(form.categoryId())
            .orElseThrow(() -> new ProductCategoryNotFoundException(form.categoryId()));
        ProductTemplate categoryTemplate = resolvedCategory.getDefaultTemplate();

        product.setSku(sku);
        product.setName(form.name().trim());
        product.setSlug(slug);
        product.setDescription(trimToNull(form.description()));
        product.setCategory(resolvedCategory);
        product.setMainImageUrl(trimToNull(form.mainImageUrl()));
        product.setActive(form.active());
        product.setVisible(form.visible());
        product.setListable(form.isListable());
        product.setSearchable(form.isPurchasable());
        product.setPurchasableAlone(form.isPurchasableAlone());
        product.setTemplate(categoryTemplate);

        return new ProductUpdateContext(product, sources);
    }

    private void ensureSkuAndSlugAreUnique(String sku, String slug) {
        if (productRepository.findBySku(sku).isPresent()) {
            throw new ProductSkuExistsException(sku);
        }
        if (productRepository.findBySlug(slug).isPresent()) {
            throw new ProductSlugExistsException(slug);
        }
    }

    private void ensureSkuAndSlugAreUniqueForUpdate(Long productId, String sku, String slug) {
        var existingBySku = productRepository.findBySku(sku);
        if (existingBySku.isPresent() && !existingBySku.get().getId().equals(productId)) {
            throw new ProductSkuExistsException(sku);
        }

        var existingBySlug = productRepository.findBySlug(slug);
        if (existingBySlug.isPresent() && !existingBySlug.get().getId().equals(productId)) {
            throw new ProductSlugExistsException(slug);
        }
    }

    private record ProductCreateContext(
        Product product,
        List<com.cookiesstore.common.entities.Source> sources
    ) {
    }

    private record ProductUpdateContext(
        Product product,
        List<com.cookiesstore.common.entities.Source> sources
    ) {
    }
}
