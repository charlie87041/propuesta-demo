package com.cookiesstore.pos.repository;

import com.cookiesstore.common.entities.Source;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PosSourceRepository extends JpaRepository<Source, Long> {
}
