package com.cookiesstore.pos.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.cookiesstore.common.entities.AdminSourcePosSession;

public interface PosSessionRepository extends JpaRepository<AdminSourcePosSession, Long>
 {
    public Optional<AdminSourcePosSession> findBySourceIdAndSessionDate(Long sourceId, LocalDate sessionDate);


    public Optional<AdminSourcePosSession> findBySourceIdAndId(Long sourceId, Long sessionId);

    Page<AdminSourcePosSession> findBySourceIdOrderBySessionDateDesc(Long sourceId, Pageable pageable);
}
