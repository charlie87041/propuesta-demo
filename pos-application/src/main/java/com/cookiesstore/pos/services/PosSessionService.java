package com.cookiesstore.pos.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.cookiesstore.common.entities.AdminSourcePosConfig;
import com.cookiesstore.common.entities.AdminSourcePosSession;
import com.cookiesstore.common.repositories.AdminSourcePosConfigRepository;
import com.cookiesstore.pos.dto.CloseSessionBreakdownForm;
import com.cookiesstore.pos.dto.CloseSessionForm;
import com.cookiesstore.pos.dto.StartSessionForm;
import com.cookiesstore.pos.repository.PosSessionRepository;

@Service
public class PosSessionService {

    private final AdminSourcePosConfigRepository sourceConfigRepository;
    private final PosSessionRepository posSessionRepository;
    private final PosSessionCashBreakdownService     posSessionBreakdownService;

    public PosSessionService(
        AdminSourcePosConfigRepository sourceConfigRepository,
        PosSessionRepository posSessionRepository,
        PosSessionCashBreakdownService posSessionBreakdownService
    ) {
        this.sourceConfigRepository = sourceConfigRepository;
        this.posSessionRepository = posSessionRepository;
        this.posSessionBreakdownService = posSessionBreakdownService;
    }

    @Transactional(readOnly = true)
    public boolean isClosedToday(Long sourceId) {
        return sourceConfigRepository.findBySourceId(sourceId)
            .map(AdminSourcePosConfig::isClosedToday)
            .orElse(true);
    }
    
    @Transactional(readOnly = true)
    public Optional<AdminSourcePosSession> findTodaySession(Long sourceId) {
        return posSessionRepository.findBySourceIdAndSessionDate(sourceId, LocalDate.now());
    }




    @Transactional(propagation = Propagation.REQUIRED)
    public AdminSourcePosSession startSession(Long sourceId, Long currentUserId, StartSessionForm form) {
        var today = LocalDate.now();
        var config = this.getSourceConfig(sourceId);

        if (posSessionRepository.findBySourceIdAndSessionDate(sourceId, today).isPresent()) {
            throw new PosSessionDomainException("pos.session.alreadyOpenToday");
        }

        var session = new AdminSourcePosSession();
        session.setSource(config.getSource());
        session.setOpenedByAdminUserId(currentUserId);
        session.setOpeningCashBalance(form.getInCashAmount());
        session.setSessionDate(today);
        session.setCashPaymentsTotalMinor(0L);
        session.setOtherPaymentsTotalMinor(0L);
        session.setTotalSalesMinor(0L);
        session.setDrawerNote(null);

        AdminSourcePosSession created = posSessionRepository.save(session);

        config.setClosedToday(false);
        sourceConfigRepository.save(config);

        return created;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public AdminSourcePosSession closeSession(Long sourceId, Long sessionId, Long currentUserId, CloseSessionForm form) {
        var today = LocalDate.now();
        var config = this.getSourceConfig(sourceId);
        if (config.isClosedToday()) {
            throw new PosSessionDomainException("pos.session.alreadyClosedToday");
        }

        var session = posSessionRepository.findBySourceIdAndId(sourceId, sessionId)
            .orElseThrow(() -> new PosSessionDomainException("pos.session.notFound"));
        if (!today.equals(session.getSessionDate())) {
            throw new PosSessionDomainException("pos.session.invalidSessionForClose");
        }
        if (session.getClosedByAdminUserId() != null) {
            throw new PosSessionDomainException("pos.session.alreadyClosed");
        }
        
        if (config.isForceCashBreakdownOnClose() && form.getBreakdowns().isEmpty()) {
            throw new PosSessionDomainException("pos.session.breakdownRequired");
            
        }

        var breakdowns = form.getBreakdowns().stream().collect(Collectors.toMap(
            CloseSessionBreakdownForm::getCurrencyBreadownId,
            CloseSessionBreakdownForm::getCurrencyBreakdonwCount,
            (existing, replacement) -> existing
        ));
        if (!breakdowns.isEmpty()) {
            if (config.getDefaultCurrency() == null || config.getDefaultCurrency().getCode() == null) {
                throw new PosSessionDomainException("pos.session.config.notFound");
            }
            posSessionBreakdownService.upsertBreakdown(sessionId, config.getDefaultCurrency().getCode(), breakdowns);
        }

        session.setDrawerNote(form.getDrawerNote() == null ? null : form.getDrawerNote().trim());
        session.setClosedByAdminUserId(currentUserId);
        posSessionRepository.save(session);

        config.setClosedToday(true);
        sourceConfigRepository.save(config);

        return session;
    }


    public AdminSourcePosConfig getSourceConfig(Long sourceId) {
        return sourceConfigRepository.findBySourceId(sourceId)
            .orElseThrow(() -> new PosSessionDomainException("pos.session.config.notFound"));
    }

    @Transactional(readOnly = true)
    public SessionHistoryPage getSessionHistory(Long sourceId, int page, int size, Locale locale) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        var result = posSessionRepository.findBySourceIdOrderBySessionDateDesc(
            sourceId,
            PageRequest.of(safePage, safeSize)
        );

        List<SessionHistoryRow> rows = result.getContent().stream().map(session -> new SessionHistoryRow(
            session.getSessionDate().toString(),
            session.getOpeningCashBalance() == null
                ? formatMoney(0L, locale)
                : formatMoney(session.getOpeningCashBalance().movePointRight(2).longValue(), locale),
            formatMoney(session.getCashPaymentsTotalMinor(), locale),
            formatMoney(session.getOtherPaymentsTotalMinor(), locale),
            formatMoney(session.getTotalSalesMinor(), locale),
            session.getDrawerNote() == null || session.getDrawerNote().isBlank() ? "N/A" : session.getDrawerNote()
        )).toList();

        return new SessionHistoryPage(
            rows,
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
    }

    private String formatMoney(Long amountMinor, Locale locale) {
        var effectiveLocale = locale == null ? Locale.US : locale;
        var format = java.text.NumberFormat.getCurrencyInstance(effectiveLocale);
        long minor = amountMinor == null ? 0L : amountMinor;
        return format.format(minor / 100d);
    }

    public record SessionHistoryRow(
        String date,
        String openingCash,
        String cashPayments,
        String otherPayments,
        String totalSale,
        String drawerNote
    ) {}

    public record SessionHistoryPage(
        List<SessionHistoryRow> rows,
        int page,
        int size,
        long totalElements,
        int totalPages
    ) {}
}
