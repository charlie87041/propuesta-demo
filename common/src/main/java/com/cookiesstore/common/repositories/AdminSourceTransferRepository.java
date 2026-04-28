package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.AdminSourceTransfer;
import com.cookiesstore.common.entities.AdminSourceTransferStatus;

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
public interface AdminSourceTransferRepository extends JpaRepository<AdminSourceTransfer, Long> {

    Page<AdminSourceTransfer> findBySourceToId(Long sourceId, Pageable pageable);

    Page<AdminSourceTransfer> findBySourceFromId(Long sourceId, Pageable pageable);

    Page<AdminSourceTransfer> findBySourceToIdAndStatus(Long sourceId, AdminSourceTransferStatus status, Pageable pageable);

    Page<AdminSourceTransfer> findBySourceFromIdAndStatus(Long sourceId, AdminSourceTransferStatus status, Pageable pageable);

    long countBySourceToId(Long sourceId);

    long countBySourceFromId(Long sourceId);

    long countBySourceToIdAndStatus(Long sourceId, AdminSourceTransferStatus status);

    long countBySourceFromIdAndStatus(Long sourceId, AdminSourceTransferStatus status);

    Optional<AdminSourceTransfer> findFirstBySourceToIdOrderByUpdatedAtDesc(Long sourceId);

    Optional<AdminSourceTransfer> findFirstBySourceFromIdOrderByUpdatedAtDesc(Long sourceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select t
        from AdminSourceTransfer t
        where t.id = :transferId
    """)
    Optional<AdminSourceTransfer> findForUpdateById(@Param("transferId") Long transferId);
}
