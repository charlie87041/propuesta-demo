package com.cookiesstore.admin.repository.pos;

import com.cookiesstore.admin.domain.pos.AdminSourcePosUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminSourcePosUserRepository extends JpaRepository<AdminSourcePosUser, Long> {

    List<AdminSourcePosUser> findBySourceIdOrderByCreatedAtDesc(Long sourceId);

    Optional<AdminSourcePosUser> findBySourceIdAndAdminUserId(Long sourceId, Long adminUserId);

    boolean existsBySourceIdAndAdminUserId(Long sourceId, Long adminUserId);
}
