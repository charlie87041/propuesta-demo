package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.AdminSourcePosSessionDenomination;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminSourcePosSessionDenominationRepository extends JpaRepository<AdminSourcePosSessionDenomination, Long> {

    List<AdminSourcePosSessionDenomination> findByPosSessionIdOrderByDenominationDisplayOrderAsc(Long posSessionId);

    Optional<AdminSourcePosSessionDenomination> findByPosSessionIdAndDenominationId(Long posSessionId, Long denominationId);
}

