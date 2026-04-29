package com.cookiesstore.admin.repository.pos;

import com.cookiesstore.admin.domain.pos.AdminSourcePosConfig;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminSourcePosConfigRepository extends JpaRepository<AdminSourcePosConfig, Long> {

    Optional<AdminSourcePosConfig> findBySourceId(Long sourceId);
}
