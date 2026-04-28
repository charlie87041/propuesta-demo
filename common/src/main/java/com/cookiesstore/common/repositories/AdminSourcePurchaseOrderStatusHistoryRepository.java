package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.AdminSourcePurchaseOrderStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminSourcePurchaseOrderStatusHistoryRepository extends JpaRepository<AdminSourcePurchaseOrderStatusHistory, Long> {

    List<AdminSourcePurchaseOrderStatusHistory> findByPurchaseOrderIdOrderByChangedAtDesc(Long purchaseOrderId);
}
