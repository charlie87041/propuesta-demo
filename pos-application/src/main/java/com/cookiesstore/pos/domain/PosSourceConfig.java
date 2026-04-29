package com.cookiesstore.pos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin_source_pos_configs")
public class PosSourceConfig {

    @Id
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private PosSource source;

    @Column(name = "pos_enabled", nullable = false)
    private boolean posEnabled;

    @Column(name = "is_closed_today", nullable = false)
    private boolean closedToday = true;

    @ManyToOne
    @JoinColumn(name = "default_currency_code")
    private PosCurrency defaultCurrency;

    public Long getId() {
        return id;
    }

    public PosSource getSource() {
        return source;
    }

    public boolean isPosEnabled() {
        return posEnabled;
    }

    public boolean isClosedToday() {
        return closedToday;
    }

    public PosCurrency getDefaultCurrency() {
        return defaultCurrency;
    }
}
