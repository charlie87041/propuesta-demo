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

    @Query("""
        select i
        from AdminSourceTransferIncidentItem i
        where (i.incident.transfer.sourceFrom.id = :sourceFromId or i.incident.transfer.sourceTo.id = :sourceToId)
          and i.archived = true
    """)
    Page<AdminSourceTransferIncidentItem> findClosedBySourceIds(
        @Param("sourceFromId") Long sourceFromId,
        @Param("sourceToId") Long sourceToId,
        Pageable pageable
    );

    @Query("""
        select i
        from AdminSourceTransferIncidentItem i
        where (i.incident.transfer.sourceFrom.id = :sourceFromId or i.incident.transfer.sourceTo.id = :sourceToId)
          and i.archived = false
          and i.revertedAt is null
    """)
    Page<AdminSourceTransferIncidentItem> findOpenBySourceIds(
        @Param("sourceFromId") Long sourceFromId,
        @Param("sourceToId") Long sourceToId,
        Pageable pageable
    );

    @Query("""
        select i
        from AdminSourceTransferIncidentItem i
        where (i.incident.transfer.sourceFrom.id = :sourceFromId or i.incident.transfer.sourceTo.id = :sourceToId)
          and i.archived = false
          and i.revertedAt is not null
    """)
    Page<AdminSourceTransferIncidentItem> findRevertedBySourceIds(
        @Param("sourceFromId") Long sourceFromId,
        @Param("sourceToId") Long sourceToId,
        Pageable pageable
    );

    long countByIncidentTransferSourceFromIdOrIncidentTransferSourceToId(
        Long sourceFromId,
        Long sourceToId
    );

    @Query("""
        select count(i)
        from AdminSourceTransferIncidentItem i
        where (i.incident.transfer.sourceFrom.id = :sourceFromId or i.incident.transfer.sourceTo.id = :sourceToId)
          and i.archived = true
    """)
    long countClosedBySourceIds(
        @Param("sourceFromId") Long sourceFromId,
        @Param("sourceToId") Long sourceToId
    );

    @Query("""
        select count(i)
        from AdminSourceTransferIncidentItem i
        where (i.incident.transfer.sourceFrom.id = :sourceFromId or i.incident.transfer.sourceTo.id = :sourceToId)
          and i.archived = false
          and i.revertedAt is null
    """)
    long countOpenBySourceIds(
        @Param("sourceFromId") Long sourceFromId,
        @Param("sourceToId") Long sourceToId
    );

    @Query("""
        select count(i)
        from AdminSourceTransferIncidentItem i
        where (i.incident.transfer.sourceFrom.id = :sourceFromId or i.incident.transfer.sourceTo.id = :sourceToId)
          and i.archived = false
          and i.revertedAt is not null
    """)
    long countRevertedBySourceIds(
        @Param("sourceFromId") Long sourceFromId,
        @Param("sourceToId") Long sourceToId
    );
}
