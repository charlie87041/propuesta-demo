package com.cookiesstore.admin.service.products;

import com.cookiesstore.admin.service.sources.SourceNotFoundException;
import com.cookiesstore.common.entities.Currency;
import com.cookiesstore.common.entities.Product;
import com.cookiesstore.common.entities.ProductSource;
import com.cookiesstore.common.entities.Source;
import com.cookiesstore.common.repositories.PriceRepository;
import com.cookiesstore.common.repositories.ProductSourceRepository;
import com.cookiesstore.common.repositories.SourceRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ProductSourceService {

    public static final int DEFAULT_LOW_STOCK_THRESHOLD = 20;

    private final SourceRepository sourceRepository;
    private final ProductSourceRepository productSourceRepository;
    private final PriceRepository priceRepository;
    private final ProductPriceService productPriceService;

    public ProductSourceService(
        SourceRepository sourceRepository,
        ProductSourceRepository productSourceRepository,
        PriceRepository priceRepository,
        ProductPriceService productPriceService
    ) {
        this.sourceRepository = sourceRepository;
        this.productSourceRepository = productSourceRepository;
        this.priceRepository = priceRepository;
        this.productPriceService = productPriceService;
    }

    public List<Source> resolveSources(List<Long> sourceIds) {
        List<Source> sources = sourceIds == null || sourceIds.isEmpty()
            ? sourceRepository.findAllBySystemManagedTrue()
            : sourceRepository.findAllById(sourceIds);
        List<Source> deduplicated = deduplicateSources(sources);
        if (deduplicated.isEmpty()) {
            throw new SourceNotFoundException(sourceIds);
        }
        return deduplicated;
    }

    public Map<Long, BigDecimal> resolveSourcePricesForSelectedSources(
        List<Source> selectedSources,
        Map<Long, BigDecimal> sourcePrices
    ) {
        Map<Long, BigDecimal> resolved = new HashMap<>();
        if (sourcePrices != null && !sourcePrices.isEmpty()) {
            resolved.putAll(sourcePrices);
        }

        if (selectedSources == null || selectedSources.isEmpty()) {
            return resolved;
        }

        for (Source source : selectedSources) {
            if (!resolved.containsKey(source.getId())) {
                resolved.put(source.getId(), null);
            }
        }
        return resolved;
    }

    public void upsertProductSources(
        Product product,
        List<Source> sources,
        Map<Long, BigDecimal> sourcePrices,
        Map<Long, Integer> sourceStockQuantities,
        Map<Long, Integer> sourceLowStockThresholds,
        Integer stockQuantity,
        Integer lowStockThreshold,
        Currency defaultCurrency,
        Long actorUserId,
        boolean sync
    ) {
        if (sync) {
            List<ProductSource> existingProductSources = productSourceRepository.findByProductId(product.getId());
            Set<Long> requestedSourceIds = new HashSet<>();
            for (Source source : sources) {
                requestedSourceIds.add(source.getId());
            }
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

        for (Source currentSource : sources) {
            ProductSource productSource = new ProductSource();
            productSource.setProduct(product);
            productSource.setSource(currentSource);
            Long sourceId = currentSource.getId();

            Integer sourceStock = sourceStockQuantities == null ? null : sourceStockQuantities.get(sourceId);
            Integer sourceThreshold = sourceLowStockThresholds == null ? null : sourceLowStockThresholds.get(sourceId);
            BigDecimal sourcePrice = sourcePrices == null ? null : sourcePrices.get(sourceId);

            productSource.setStockQuantity(sourceStock != null ? sourceStock : (stockQuantity != null ? stockQuantity : 0));
            productSource.setLowStockThreshold(
                sourceThreshold != null
                    ? sourceThreshold
                    : (lowStockThreshold != null ? lowStockThreshold : DEFAULT_LOW_STOCK_THRESHOLD)
            );

            productSource.setPrice(productPriceService.upsertSourcePrice(
                product,
                currentSource,
                sourcePrice,
                defaultCurrency,
                actorUserId
            ));
            productSourceRepository.save(productSource);
        }
    }

    public ProductSourceSnapshot buildUpdateSourceSnapshot(Long productId) {
        List<ProductSource> productSources = productSourceRepository.findByProductId(productId);

        List<Long> sourceIds = productSources.stream()
            .map(ps -> ps.getSource().getId())
            .toList();

        Map<Long, BigDecimal> sourcePrices = new HashMap<>();
        Map<Long, Integer> sourceStockQuantities = new HashMap<>();
        Map<Long, Integer> sourceLowStockThresholds = new HashMap<>();

        int defaultStockQuantity = 0;
        int defaultLowStockThreshold = DEFAULT_LOW_STOCK_THRESHOLD;
        if (!productSources.isEmpty()) {
            defaultStockQuantity = productSources.get(0).getStockQuantity();
            defaultLowStockThreshold = productSources.get(0).getLowStockThreshold();
        }

        for (ProductSource productSource : productSources) {
            Long sourceId = productSource.getSource().getId();
            sourceStockQuantities.put(sourceId, productSource.getStockQuantity());
            sourceLowStockThresholds.put(sourceId, productSource.getLowStockThreshold());

            if (productSource.getPrice() != null && productSource.getPrice().getAmount() != null) {
                sourcePrices.put(sourceId, productSource.getPrice().getAmount());
            }
        }

        return new ProductSourceSnapshot(
            sourceIds,
            sourcePrices,
            sourceStockQuantities,
            sourceLowStockThresholds,
            defaultStockQuantity,
            defaultLowStockThreshold
        );
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

    public record ProductSourceSnapshot(
        List<Long> sourceIds,
        Map<Long, BigDecimal> sourcePrices,
        Map<Long, Integer> sourceStockQuantities,
        Map<Long, Integer> sourceLowStockThresholds,
        int defaultStockQuantity,
        int defaultLowStockThreshold
    ) {
    }
}
