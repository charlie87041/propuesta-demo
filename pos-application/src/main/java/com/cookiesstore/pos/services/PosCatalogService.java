package com.cookiesstore.pos.services;

import com.cookiesstore.pos.dto.PosCatalogItemView;
import com.cookiesstore.pos.repository.PosCatalogRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PosCatalogService {

    private final PosCatalogRepository posCatalogRepository;

    public PosCatalogService(PosCatalogRepository posCatalogRepository) {
        this.posCatalogRepository = posCatalogRepository;
    }

    public CatalogPage listBySource(Long sourceId, int offset, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        int safeOffset = Math.max(offset, 0);
        int page = safeOffset / safeLimit;

        var pageable = PageRequest.of(page, safeLimit, Sort.by(Sort.Direction.ASC, "id"));
        var rows = posCatalogRepository.findCatalogBySourceId(sourceId, pageable);

        List<PosCatalogItemView> items = rows.getContent().stream()
            .map(row -> new PosCatalogItemView(
                row.getProductId(),
                row.getProductName(),
                row.getProductTypeCode(),
                toMajor(row.getUnitPriceMinor()),
                row.getUnitPriceMinor() == null ? 0L : row.getUnitPriceMinor(),
                row.getStockQuantity() == null ? 0 : row.getStockQuantity(),
                row.getLowStockThreshold() == null ? 0 : row.getLowStockThreshold()
            ))
            .toList();

        int consumed = safeOffset + items.size();
        boolean hasMore = consumed < rows.getTotalElements();

        return new CatalogPage(items, hasMore, consumed);
    }

    private double toMajor(Long amountMinor) {
        if (amountMinor == null) {
            return 0d;
        }
        return amountMinor / 100d;
    }

    public record CatalogPage(List<PosCatalogItemView> items, boolean hasMore, int nextOffset) {
    }
}
