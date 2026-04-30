package com.cookiesstore.pos.repository;

import com.cookiesstore.common.entities.ProductSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PosCatalogRepository extends JpaRepository<ProductSource, Long> {

    interface PosCatalogItemRow {
        Long getProductId();

        String getProductName();

        String getProductTypeCode();

        Long getUnitPriceMinor();

        Integer getStockQuantity();

        Integer getLowStockThreshold();
    }

    @Query(
        value = """
            select
                p.id as productId,
                p.name as productName,
                p.productTypeCode as productTypeCode,
                coalesce(sp.amountMinor, cp.amountMinor, 0) as unitPriceMinor,
                ps.stockQuantity as stockQuantity,
                ps.lowStockThreshold as lowStockThreshold
            from ProductSource ps
            join ps.product p
            left join ps.price sp
            left join p.currentPrice cp
            where ps.source.id = :sourceId
              and ps.status = com.cookiesstore.common.entities.ProductSourceStatus.ACTIVE
              and p.active = true
              and p.visible = true
              and (p.listable is null or p.listable = true)
            order by ps.totalSold desc, p.name asc
            """,
        countQuery = """
            select count(ps.id)
            from ProductSource ps
            join ps.product p
            where ps.source.id = :sourceId
              and ps.status = com.cookiesstore.common.entities.ProductSourceStatus.ACTIVE
              and p.active = true
              and p.visible = true
              and (p.listable is null or p.listable = true)
            """
    )
    Page<PosCatalogItemRow> findCatalogBySourceId(@Param("sourceId") Long sourceId, Pageable pageable);
}
