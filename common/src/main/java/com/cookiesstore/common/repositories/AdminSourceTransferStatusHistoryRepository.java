package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.AdminSourceTransferStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminSourceTransferStatusHistoryRepository extends JpaRepository<AdminSourceTransferStatusHistory, Long> {

    List<AdminSourceTransferStatusHistory> findByTransferIdOrderByChangedAtDesc(Long transferId);
}
