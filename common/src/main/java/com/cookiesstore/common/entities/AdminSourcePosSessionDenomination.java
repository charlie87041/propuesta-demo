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
import java.time.Instant;

@Entity
@Table(
    name = "admin_source_pos_session_denominations",
    indexes = {
        @Index(name = "idx_admin_source_pos_session_denominations_session", columnList = "pos_session_id"),
        @Index(name = "idx_admin_source_pos_session_denominations_denomination", columnList = "denomination_id")
    }
)
public class AdminSourcePosSessionDenomination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pos_session_id", nullable = false)
    private AdminSourcePosSession posSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "denomination_id", nullable = false)
    private AdminSourceCurrencyDenomination denomination;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

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

    public AdminSourcePosSession getPosSession() {
        return posSession;
    }

    public void setPosSession(AdminSourcePosSession posSession) {
        this.posSession = posSession;
    }

    public AdminSourceCurrencyDenomination getDenomination() {
        return denomination;
    }

    public void setDenomination(AdminSourceCurrencyDenomination denomination) {
        this.denomination = denomination;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

