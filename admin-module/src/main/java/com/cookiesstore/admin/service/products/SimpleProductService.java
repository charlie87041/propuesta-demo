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
import com.cookiesstore.common.services.products.ProductTypeService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class SimpleProductService implements ProductTypeService<CreateProductForm, UpdateProductForm> {

    public static final String TYPE_CODE = "SIMPLE";

    protected static final Integer PRODUCT_SOURCE_THRESHOLD = 20;

    protected final ProductRepository productRepository;
    protected final CategoryRepository categoryRepository;
    protected final SourceRepository sourceRepository;
    protected final PriceRepository priceRepository;
    protected final ProductSourceRepository productSourceRepository;
    protected final PricingProperties pricingProperties;
    protected final AuthenticatedUserProvider authenticatedUserProvider;

    public SimpleProductService(
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

    @Override
    public String productTypeCode() {
        return TYPE_CODE;
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
        source = deduplicateSources(source);


        if (source.isEmpty()) {
            throw  new SourceNotFoundException(form.sourceIds());
        }

        var category = categoryRepository.findById(form.categoryId())
            .orElseThrow(() -> new ProductCategoryNotFoundException(form.categoryId()));

        Product product = new Product();
        product.setProductTypeCode(form.productTypeCode());
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
        product.setListable(form.isListable());
        product.setSearchable(form.isPurchasable());
        product.setPurchasableAlone(form.isPurchasableAlone());

        try {
            productRepository.save(product);
            upsertProductCurrentPrice(product, form.price());
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
            throw mapDataIntegrityViolation(ex);
        }
    }

    @Transactional(readOnly = true)
    public UpdateProductForm buildUpdateProductForm(Long productId) {
        Product product = getProduct(productId);
        List<ProductSource> productSources = productSourceRepository.findByProductId(productId);

        List<Long> sourceIds = productSources.stream()
            .map(ps -> ps.getSource().getId())
            .toList();

        Map<Long, Double> sourcePrices = new HashMap<>();
        Map<Long, Integer> sourceStockQuantities = new HashMap<>();
        Map<Long, Integer> sourceLowStockThresholds = new HashMap<>();

        Integer defaultStockQuantity = 0;
        Integer defaultLowStockThreshold = PRODUCT_SOURCE_THRESHOLD;
        if (!productSources.isEmpty()) {
            defaultStockQuantity = productSources.get(0).getStockQuantity();
            defaultLowStockThreshold = productSources.get(0).getLowStockThreshold();
        }

        for (ProductSource productSource : productSources) {
            Long sourceId = productSource.getSource().getId();
            sourceStockQuantities.put(sourceId, productSource.getStockQuantity());
            sourceLowStockThresholds.put(sourceId, productSource.getLowStockThreshold());

            if (productSource.getPrice() != null && productSource.getPrice().getAmount() != null) {
                sourcePrices.put(sourceId, productSource.getPrice().getAmount().doubleValue());
            }
        }

        Double currentPrice = product.getCurrentPrice() != null && product.getCurrentPrice().getAmount() != null
            ? product.getCurrentPrice().getAmount().doubleValue()
            : 0D;

        return new UpdateProductForm(
            product.getSku(),
            product.getName(),
            product.getSlug(),
            product.getDescription(),
            product.getCategory().getId(),
            product.getMainImageUrl(),
            product.getIngredients(),
            product.getAllergenInfo(),
            product.getNutritionFacts(),
            sourceIds,
            defaultStockQuantity,
            defaultLowStockThreshold,
            currentPrice,
            product.getProductTypeCode(),
            List.of(),
            List.of(),
            sourcePrices,
            sourceStockQuantities,
            sourceLowStockThresholds,
            product.isActive(),
            product.isVisible(),
            product.getListable() == null || product.getListable(),
            product.getSearchable() == null || product.getSearchable(),
            product.getPurchasableAlone() == null || product.getPurchasableAlone(),
            null,
            List.of()
        );
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
            List<ProductSource> existingProductSources = productSourceRepository.findByProductId(product.getId());
            Set<Long> requestedSourceIds = new HashSet<>();
            source.forEach(currentSource -> requestedSourceIds.add(currentSource.getId()));
            List<Long> removedSourceIds = existingProductSources.stream()
                .map(productSource -> productSource.getSource().getId())
                .filter(existingSourceId -> !requestedSourceIds.contains(existingSourceId))
                .distinct()
                .toList();

            productSourceRepository.deleteByProductId(product.getId());
            productSourceRepository.flush();

            if (!removedSourceIds.isEmpty()) {
                priceRepository.deleteByProductIdAndSourceIdIn(product.getId(), removedSourceIds);
            }
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

                productSource.setPrice(upsertSourcePrice(product, currentSource, sourcePrice, defaultCurrency, actorUserId));
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

        List<Source> source = form.sourceIds() == null || form.sourceIds().isEmpty()
            ? sourceRepository.findAllBySystemManagedTrue()
            : sourceRepository.findAllById(form.sourceIds());
        source = deduplicateSources(source);

        if (source.isEmpty()) {
            throw new SourceNotFoundException(form.sourceIds());
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
        product.setListable(form.isListable());
        product.setSearchable(form.isPurchasable());
        product.setPurchasableAlone(form.isPurchasableAlone());

        try {
            upsertProductCurrentPrice(product, form.price());
            addProductSource(
                product,
                source,
                form.sourcePrices(),
                form.sourceStockQuantities(),
                form.sourceLowStockThresholds(),
                form.stockQuantity(),
                form.lowStockThreshold(),
                true
            );
            return productRepository.save(product);
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

    private String resolveDefaultCurrency() {
        String configuredCurrency = trimToNull(pricingProperties.getDefaultCurrency());
        if (configuredCurrency == null || configuredCurrency.length() != 3) {
            throw new IllegalStateException("Invalid admin.pricing.default-currency configuration");
        }
        return configuredCurrency.toUpperCase();
    }

    private void upsertProductCurrentPrice(Product product, Double amount) {
        if (amount == null) {
            return;
        }

        String currency = resolveDefaultCurrency();
        Price openBasePrice = priceRepository
            .findFirstByProductIdAndSourceIdIsNullAndCurrencyAndValidToIsNull(product.getId(), currency)
            .orElse(null);

        BigDecimal requestedAmount = BigDecimal.valueOf(amount);
        if (openBasePrice != null
            && openBasePrice.getAmount() != null
            && openBasePrice.getAmount().compareTo(requestedAmount) == 0) {
            product.setCurrentPrice(openBasePrice);
            return;
        }

        if (openBasePrice != null) {
            openBasePrice.setValidTo(Instant.now());
            priceRepository.save(openBasePrice);
        }

        Price currentPrice = newProductPrice(product, amount);
        product.setCurrentPrice(currentPrice);
    }

    private Price upsertSourcePrice(
        Product product,
        Source source,
        Double amount,
        String currency,
        Long actorUserId
    ) {
        Price openSourcePrice = priceRepository
            .findFirstByProductIdAndSourceIdAndCurrencyAndValidToIsNull(product.getId(), source.getId(), currency)
            .orElse(null);

        if (amount == null) {
            if (openSourcePrice != null) {
                openSourcePrice.setValidTo(Instant.now());
                priceRepository.save(openSourcePrice);
            }
            return null;
        }

        BigDecimal requestedAmount = BigDecimal.valueOf(amount);
        if (openSourcePrice != null
            && openSourcePrice.getAmount() != null
            && openSourcePrice.getAmount().compareTo(requestedAmount) == 0) {
            return openSourcePrice;
        }

        if (openSourcePrice != null) {
            openSourcePrice.setValidTo(Instant.now());
            priceRepository.save(openSourcePrice);
        }

        Price price = new Price();
        price.setSource(source);
        price.setAmount(requestedAmount);
        price.setCurrency(currency);
        price.setProduct(product);
        price.setValidFrom(Instant.now());
        price.setCreatedBy(actorUserId);
        return priceRepository.save(price);
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

    private List<Source> deduplicateSources(List<Source> sources) {
        if (sources == null || sources.isEmpty()) {
            return List.of();
        }
        return new java.util.ArrayList<>(
            sources.stream()
                .collect(
                    java.util.stream.Collectors.toMap(
                        Source::getId,
                        source -> source,
                        (left, right) -> left,
                        LinkedHashMap::new
                    )
                )
                .values()
        );
    }
}
