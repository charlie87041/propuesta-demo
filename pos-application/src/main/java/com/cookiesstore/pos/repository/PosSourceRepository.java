package com.cookiesstore.pos.repository;

import com.cookiesstore.pos.domain.PosSource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PosSourceRepository extends JpaRepository<PosSource, Long> {
}
