package com.cookiesstore.admin.repository.pos;

import com.cookiesstore.common.entities.AdminSourcePosSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminSourcePosSessionRepository extends JpaRepository<AdminSourcePosSession, Long> {

    Optional<AdminSourcePosSession> findBySourceIdAndSessionDate(Long sourceId, LocalDate sessionDate);

    List<AdminSourcePosSession> findBySourceIdOrderBySessionDateDesc(Long sourceId);
}
