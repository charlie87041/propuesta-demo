package com.cookiesstore.pos.repository;

import com.cookiesstore.pos.domain.PosUserAssignment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PosUserAssignmentRepository extends JpaRepository<PosUserAssignment, Long> {

    Optional<PosUserAssignment> findBySourceIdAndAdminUserIdAndActiveTrue(Long sourceId, Long adminUserId);
}
