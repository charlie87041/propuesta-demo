package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.AdminSourcePurchaseOrderItem;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminSourcePurchaseOrderItemRepository extends JpaRepository<AdminSourcePurchaseOrderItem, Long> {

    interface PurchaseOrderTotalProjection {
        Long getPurchaseOrderId();

        Long getTotalMinor();
    }

    List<AdminSourcePurchaseOrderItem> findByPurchaseOrderId(Long purchaseOrderId);

    @Query("""
        select
            i.purchaseOrder.id as purchaseOrderId,
            coalesce(sum(i.lineTotalMinor), 0) as totalMinor
        from AdminSourcePurchaseOrderItem i
        where i.purchaseOrder.id in :purchaseOrderIds
        group by i.purchaseOrder.id
    """)
    List<PurchaseOrderTotalProjection> sumTotalsByPurchaseOrderIds(@Param("purchaseOrderIds") Collection<Long> purchaseOrderIds);

    void deleteByPurchaseOrderId(Long purchaseOrderId);
}
