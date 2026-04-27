package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.AdminSourceTransferIncidentItem;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminSourceTransferIncidentItemRepository extends JpaRepository<AdminSourceTransferIncidentItem, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select i
        from AdminSourceTransferIncidentItem i
        join fetch i.incident incident
        join fetch incident.transfer transfer
        left join fetch i.product product
        where i.id = :incidentItemId
    """)
    Optional<AdminSourceTransferIncidentItem> findForUpdateById(@Param("incidentItemId") Long incidentItemId);

    Page<AdminSourceTransferIncidentItem> findByIncidentTransferSourceFromIdOrIncidentTransferSourceToId(
        Long sourceFromId,
        Long sourceToId,
        Pageable pageable
    );
}
