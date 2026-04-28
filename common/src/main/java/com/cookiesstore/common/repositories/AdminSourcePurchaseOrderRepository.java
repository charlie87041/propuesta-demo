package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.AdminSourcePurchaseOrder;
import com.cookiesstore.common.entities.AdminSourcePurchaseOrderStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminSourcePurchaseOrderRepository extends JpaRepository<AdminSourcePurchaseOrder, Long> {

    Page<AdminSourcePurchaseOrder> findBySourceId(Long sourceId, Pageable pageable);

    List<AdminSourcePurchaseOrder> findBySourceIdOrderByCreatedAtDesc(Long sourceId);

    List<AdminSourcePurchaseOrder> findBySourceIdAndStatusOrderByCreatedAtDesc(
        Long sourceId,
        AdminSourcePurchaseOrderStatus status
    );

    Optional<AdminSourcePurchaseOrder> findByIdAndSourceId(Long id, Long sourceId);

    long countBySourceId(Long sourceId);

    long countBySourceIdAndStatus(Long sourceId, AdminSourcePurchaseOrderStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select po
        from AdminSourcePurchaseOrder po
        where po.id = :purchaseOrderId
    """)
    Optional<AdminSourcePurchaseOrder> findForUpdateById(@Param("purchaseOrderId") Long purchaseOrderId);
}
