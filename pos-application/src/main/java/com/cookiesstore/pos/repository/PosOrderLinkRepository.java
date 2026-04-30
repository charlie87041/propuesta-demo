package com.cookiesstore.pos.repository;

import com.cookiesstore.common.entities.AdminSourcePosOrder;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PosOrderLinkRepository extends JpaRepository<AdminSourcePosOrder, Long> {
    Optional<AdminSourcePosOrder> findBySourceIdAndOrderId(Long sourceId, Long orderId);
}
