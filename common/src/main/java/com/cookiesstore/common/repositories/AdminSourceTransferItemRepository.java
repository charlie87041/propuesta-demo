package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.AdminSourceTransferStatus;
import com.cookiesstore.common.entities.AdminSourceTransferItem;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminSourceTransferItemRepository extends JpaRepository<AdminSourceTransferItem, Long> {

    List<AdminSourceTransferItem> findByTransferId(Long transferId);

    @Query("""
        select coalesce(sum(i.quantity), 0)
        from AdminSourceTransferItem i
        where i.product.id = :productId
          and i.transfer.sourceFrom.id = :sourceId
          and i.transfer.status in :statuses
    """)
    int sumQuantityByProductAndSourceAndTransferStatuses(
        @Param("productId") Long productId,
        @Param("sourceId") Long sourceId,
        @Param("statuses") Collection<AdminSourceTransferStatus> statuses
    );
}
