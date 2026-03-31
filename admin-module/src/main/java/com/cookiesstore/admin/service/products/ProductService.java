package com.cookiesstore.admin.service.products;

import com.cookiesstore.admin.config.PricingProperties;
import com.cookiesstore.admin.service.sources.SourceNotFoundException;
import com.cookiesstore.admin.service.support.AuthenticatedUserProvider;
import com.cookiesstore.admin.web.dto.products.CreateProductForm;
import com.cookiesstore.admin.web.dto.products.UpdateProductForm;
import com.cookiesstore.common.entities.Price;
import com.cookiesstore.common.entities.Product;
import com.cookiesstore.common.entities.ProductSource;
import com.cookiesstore.common.entities.Source;
import com.cookiesstore.common.repositories.CategoryRepository;
import com.cookiesstore.common.repositories.PriceRepository;
import com.cookiesstore.common.repositories.ProductRepository;
import com.cookiesstore.common.repositories.ProductSourceRepository;
import com.cookiesstore.common.repositories.SourceRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class ProductService {

    private static final Integer PRODUCT_SOURCE_THRESHOLD = 20;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SourceRepository sourceRepository;
    private final PriceRepository priceRepository;
    private final ProductSourceRepository productSourceRepository;
    private final PricingProperties pricingProperties;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public ProductService(
        ProductRepository productRepository,
        CategoryRepository categoryRepository,
        SourceRepository sourceRepository,
        PriceRepository priceRepository,
        ProductSourceRepository productSourceRepository,
        PricingProperties pricingProperties,
        AuthenticatedUserProvider authenticatedUserProvider
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.sourceRepository = sourceRepository;
        this.priceRepository = priceRepository;
        this.productSourceRepository = productSourceRepository;
        this.pricingProperties = pricingProperties;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Transactional(readOnly = true)
    public Product getProduct(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    public Product createProduct(CreateProductForm form) {
        String sku = form.sku().trim();
        String slug = form.slug().trim();

        if (productRepository.findBySku(sku).isPresent()) {
            throw new ProductSkuExistsException(sku);
        }
        if (productRepository.findBySlug(slug).isPresent()) {
            throw new ProductSlugExistsException(slug);
        }
        List<Source> source = form.sourceIds() == null || form.sourceIds().isEmpty()
            ? sourceRepository.findAllBySystemManagedTrue()
            : sourceRepository.findAllById(form.sourceIds());


        if (source.isEmpty()) {
            throw  new SourceNotFoundException(form.sourceIds());
        }

        var category = categoryRepository.findById(form.categoryId())
            .orElseThrow(() -> new ProductCategoryNotFoundException(form.categoryId()));

        Product product = new Product();
        product.setSku(sku);
        product.setName(form.name().trim());
        product.setSlug(slug);
        product.setDescription(trimToNull(form.description()));
        product.setCategory(category);
        product.setMainImageUrl(trimToNull(form.mainImageUrl()));
        product.setIngredients(trimToNull(form.ingredients()));
        product.setAllergenInfo(trimToNull(form.allergenInfo()));
        product.setNutritionFacts(trimToNull(form.nutritionFacts()));
        product.setActive(form.active());
        product.setVisible(form.visible());

        try {
            productRepository.save(product);
            Price currentPrice = newProductPrice(product, form.price());
            product.setCurrentPrice(currentPrice);
            addProductSource(
                product,
                source,
                form.sourcePrices(),
                form.sourceStockQuantities(),
                form.sourceLowStockThresholds(),
                form.stockQuantity(),
                form.lowStockThreshold(),
                false
            );
            return productRepository.save(product);
        } catch (DataIntegrityViolationException ex) {
            throw new ProductUniqueConstraintException();
        }
    }

    protected Price newProductPrice(Product product, Double amount) {
        String defaultCurrency = resolveDefaultCurrency();
        Long actorUserId = authenticatedUserProvider.currentUserId();

        Price price = new Price();
        price.setAmount(BigDecimal.valueOf(amount));
        price.setCurrency(defaultCurrency);
        price.setProduct(product);
        price.setValidFrom(Instant.now());
        price.setCreatedBy(actorUserId);
        return priceRepository.save(price);
    }


    protected void addProductSource(
        Product product,
        List<Source> source,
        Map<Long, Double> sourcePrices,
        Map<Long, Integer> sourceStockQuantities,
        Map<Long, Integer> sourceLowStockThresholds,
        Integer stockQuantity,
        Integer lowStockThreshold,
        boolean sync
    ) {
        String defaultCurrency = resolveDefaultCurrency();
        Long actorUserId = authenticatedUserProvider.currentUserId();

        if (sync) {
            productSourceRepository.deleteByProductId(product.getId());
        }
        source.stream()
            .forEach((Source currentSource) -> {
                ProductSource productSource = new ProductSource();
                productSource.setProduct(product);
                productSource.setSource(currentSource);
                Long sourceId = currentSource.getId();

                Integer sourceStock = sourceStockQuantities == null ? null : sourceStockQuantities.get(sourceId);
                Integer sourceThreshold = sourceLowStockThresholds == null ? null : sourceLowStockThresholds.get(sourceId);
                Double sourcePrice = sourcePrices == null ? null : sourcePrices.get(sourceId);

                productSource.setStockQuantity(sourceStock != null ? sourceStock : (stockQuantity != null ? stockQuantity : 0));
                productSource.setLowStockThreshold(
                    sourceThreshold != null ? sourceThreshold : (lowStockThreshold != null ? lowStockThreshold : PRODUCT_SOURCE_THRESHOLD)
                );

                if (sourcePrice != null) {
                    Price price = new Price();
                    price.setSource(currentSource);
                    price.setAmount(BigDecimal.valueOf(sourcePrice));
                    price.setCurrency(defaultCurrency);
                    price.setProduct(product);
                    price.setValidFrom(Instant.now());
                    price.setCreatedBy(actorUserId);
                    productSource.setPrice(priceRepository.save(price));
                }
                productSourceRepository.save(productSource);
            });
    }

    public Product updateProduct(Long productId, UpdateProductForm form) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

        String sku = form.sku().trim();
        String slug = form.slug().trim();

        var existingBySku = productRepository.findBySku(sku);
        if (existingBySku.isPresent() && !existingBySku.get().getId().equals(productId)) {
            throw new ProductSkuExistsException(sku);
        }

        var existingBySlug = productRepository.findBySlug(slug);
        if (existingBySlug.isPresent() && !existingBySlug.get().getId().equals(productId)) {
            throw new ProductSlugExistsException(slug);
        }

        var category = categoryRepository.findById(form.categoryId())
            .orElseThrow(() -> new ProductCategoryNotFoundException(form.categoryId()));

        product.setSku(sku);
        product.setName(form.name().trim());
        product.setSlug(slug);
        product.setDescription(trimToNull(form.description()));
        product.setCategory(category);
        product.setMainImageUrl(trimToNull(form.mainImageUrl()));
        product.setIngredients(trimToNull(form.ingredients()));
        product.setAllergenInfo(trimToNull(form.allergenInfo()));
        product.setNutritionFacts(trimToNull(form.nutritionFacts()));
        product.setActive(form.active());
        product.setVisible(form.visible());

        try {
            return productRepository.save(product);
        } catch (DataIntegrityViolationException ex) {
            throw new ProductUniqueConstraintException();
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

    private String resolveDefaultCurrency() {
        String configuredCurrency = trimToNull(pricingProperties.getDefaultCurrency());
        if (configuredCurrency == null || configuredCurrency.length() != 3) {
            throw new IllegalStateException("Invalid admin.pricing.default-currency configuration");
        }
        return configuredCurrency.toUpperCase();
    }
}
