package com.cookiesstore.pos.repository;

import com.cookiesstore.pos.domain.PosSourceConfig;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PosSourceConfigRepository extends JpaRepository<PosSourceConfig, Long> {

    Optional<PosSourceConfig> findBySourceId(Long sourceId);
}
