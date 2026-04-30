package com.cookiesstore.pos.services;

import com.cookiesstore.common.entities.AdminSourcePosSessionDenomination;
import com.cookiesstore.common.entities.AdminSourceCurrencyDenomination;
import com.cookiesstore.common.repositories.AdminSourceCurrencyDenominationRepository;
import com.cookiesstore.common.repositories.AdminSourcePosSessionDenominationRepository;
import com.cookiesstore.pos.repository.PosSessionRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PosSessionCashBreakdownService {

    private final PosSessionRepository posSessionRepository;
    private final AdminSourceCurrencyDenominationRepository denominationRepository;
    private final AdminSourcePosSessionDenominationRepository sessionDenominationRepository;

    public PosSessionCashBreakdownService(
        PosSessionRepository posSessionRepository,
        AdminSourceCurrencyDenominationRepository denominationRepository,
        AdminSourcePosSessionDenominationRepository sessionDenominationRepository
    ) {
        this.posSessionRepository = posSessionRepository;
        this.denominationRepository = denominationRepository;
        this.sessionDenominationRepository = sessionDenominationRepository;
    }

    @Transactional
    public void upsertBreakdown(Long posSessionId, String currencyCode, Map<Long, Integer> quantitiesByDenominationId) {
        var session = posSessionRepository.findById(posSessionId)
            .orElseThrow(() -> new IllegalArgumentException("POS session not found: " + posSessionId));

        if (quantitiesByDenominationId == null || quantitiesByDenominationId.isEmpty()) {
            sessionDenominationRepository.deleteAll(
                sessionDenominationRepository.findByPosSessionIdOrderByDenominationDisplayOrderAsc(posSessionId)
            );
            return;
        }

        List<AdminSourceCurrencyDenomination> allowedDenominations = findActiveDenominationsByCurrency(currencyCode);
        Set<Long> allowedIds = allowedDenominations.stream()
            .map(AdminSourceCurrencyDenomination::getId)
            .collect(Collectors.toSet());
        Map<Long, AdminSourceCurrencyDenomination> denominationById = allowedDenominations.stream()
            .collect(Collectors.toMap(AdminSourceCurrencyDenomination::getId, denomination -> denomination));

        Map<Long, AdminSourcePosSessionDenomination> existingByDenominationId = new HashMap<>();
        for (AdminSourcePosSessionDenomination existing : sessionDenominationRepository
            .findByPosSessionIdOrderByDenominationDisplayOrderAsc(posSessionId)) {
            existingByDenominationId.put(existing.getDenomination().getId(), existing);
        }

        for (Map.Entry<Long, Integer> entry : quantitiesByDenominationId.entrySet()) {
            Long denominationId = entry.getKey();
            Integer quantity = entry.getValue() == null ? 0 : entry.getValue();
            if (denominationId == null || !allowedIds.contains(denominationId)) {
                throw new PosSessionDomainException("pos.session.breakdownInvalidDenomination");
            }

            var denomination = denominationById.get(denominationId);

            if (quantity <= 0) {
                AdminSourcePosSessionDenomination existingRow = existingByDenominationId.get(denominationId);
                if (existingRow != null) {
                    sessionDenominationRepository.delete(existingRow);
                }
                continue;
            }

            AdminSourcePosSessionDenomination row = existingByDenominationId.get(denominationId);
            if (row == null) {
                row = new AdminSourcePosSessionDenomination();
                row.setPosSession(session);
                row.setDenomination(denomination);
            }

            row.setQuantity(quantity);
            sessionDenominationRepository.save(row);
        }
    }

    @Transactional(readOnly = true)
    public List<AdminSourcePosSessionDenomination> findBySession(Long posSessionId) {
        return sessionDenominationRepository.findByPosSessionIdOrderByDenominationDisplayOrderAsc(posSessionId);
    }

    @Transactional(readOnly = true)
    public List<AdminSourceCurrencyDenomination> findActiveDenominationsByCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return List.of();
        }
        return denominationRepository.findByCurrencyCodeAndActiveTrueOrderByDisplayOrderAsc(currencyCode);
    }
}
