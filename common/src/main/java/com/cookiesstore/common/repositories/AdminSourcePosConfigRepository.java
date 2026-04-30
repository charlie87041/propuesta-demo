package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.AdminSourcePosConfig;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AdminSourcePosConfigRepository extends JpaRepository<AdminSourcePosConfig, Long> {

    Optional<AdminSourcePosConfig> findBySourceId(Long sourceId);

    @Modifying
    @Query("""
        UPDATE AdminSourcePosConfig config
        SET config.closedToday = true
        WHERE config.closedToday = false
          AND config.posEnabled = true
        """)
    int closeAllOpenSessionsForToday();
}

