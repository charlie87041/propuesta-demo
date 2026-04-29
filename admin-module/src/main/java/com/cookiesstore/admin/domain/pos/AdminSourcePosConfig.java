package com.cookiesstore.admin.domain.pos;

import com.cookiesstore.common.entities.Currency;
import com.cookiesstore.common.entities.Source;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "admin_source_pos_configs")
public class AdminSourcePosConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id", nullable = false, unique = true)
    private Source source;

    @Column(name = "pos_enabled", nullable = false)
    private boolean posEnabled = false;

    @Column(name = "is_closed_today", nullable = false)
    private boolean closedToday = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_currency_code")
    private Currency defaultCurrency;

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

    public boolean isPosEnabled() {
        return posEnabled;
    }

    public void setPosEnabled(boolean posEnabled) {
        this.posEnabled = posEnabled;
    }

    public Currency getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(Currency defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public boolean isClosedToday() {
        return closedToday;
    }

    public void setClosedToday(boolean closedToday) {
        this.closedToday = closedToday;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
