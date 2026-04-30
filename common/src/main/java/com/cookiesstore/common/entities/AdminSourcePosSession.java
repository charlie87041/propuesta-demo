package com.cookiesstore.common.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "admin_source_pos_sessions",
    indexes = {
        @Index(name = "idx_admin_source_pos_sessions_source_date", columnList = "source_id,session_date")
    }
)
public class AdminSourcePosSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "opened_by_admin_user_id")
    private Long openedByAdminUserId;

    @Column(name = "closed_by_admin_user_id")
    private Long closedByAdminUserId;

    @Column(name = "opening_cash_balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal openingCashBalance = BigDecimal.ZERO;

    @Column(name = "cash_payments_total_minor", nullable = false)
    private Long cashPaymentsTotalMinor = 0L;

    @Column(name = "other_payments_total_minor", nullable = false)
    private Long otherPaymentsTotalMinor = 0L;

    @Column(name = "total_sales_minor", nullable = false)
    private Long totalSalesMinor = 0L;

    @Column(name = "drawer_note", length = 500)
    private String drawerNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public Long getOpenedByAdminUserId() {
        return openedByAdminUserId;
    }

    public void setOpenedByAdminUserId(Long openedByAdminUserId) {
        this.openedByAdminUserId = openedByAdminUserId;
    }

    public Long getClosedByAdminUserId() {
        return closedByAdminUserId;
    }

    public void setClosedByAdminUserId(Long closedByAdminUserId) {
        this.closedByAdminUserId = closedByAdminUserId;
    }

    public BigDecimal getOpeningCashBalance() {
        return openingCashBalance;
    }

    public void setOpeningCashBalance(BigDecimal openingCashBalance) {
        this.openingCashBalance = openingCashBalance;
    }

    public Long getCashPaymentsTotalMinor() {
        return cashPaymentsTotalMinor;
    }

    public void setCashPaymentsTotalMinor(Long cashPaymentsTotalMinor) {
        this.cashPaymentsTotalMinor = cashPaymentsTotalMinor;
    }

    public Long getOtherPaymentsTotalMinor() {
        return otherPaymentsTotalMinor;
    }

    public void setOtherPaymentsTotalMinor(Long otherPaymentsTotalMinor) {
        this.otherPaymentsTotalMinor = otherPaymentsTotalMinor;
    }

    public Long getTotalSalesMinor() {
        return totalSalesMinor;
    }

    public void setTotalSalesMinor(Long totalSalesMinor) {
        this.totalSalesMinor = totalSalesMinor;
    }

    public String getDrawerNote() {
        return drawerNote;
    }

    public void setDrawerNote(String drawerNote) {
        this.drawerNote = drawerNote;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
