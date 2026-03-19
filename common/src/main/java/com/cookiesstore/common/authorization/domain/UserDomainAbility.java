package com.cookiesstore.common.authorization.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(
    name = "user_domain_abilities",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_domain_ability", columnNames = {"user_id", "domain_id", "ability_id"})
    },
    indexes = {
        @Index(name = "idx_uda_user_id", columnList = "user_id"),
        @Index(name = "idx_uda_domain_id", columnList = "domain_id"),
        @Index(name = "idx_uda_lookup", columnList = "user_id,domain_id,granted")
    }
)
public class UserDomainAbility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "domain_id", nullable = false)
    private Domain domain;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ability_id", nullable = false)
    private Ability ability;

    @Column(nullable = false)
    private boolean granted = true;

    @Column(name = "granted_by")
    private Long grantedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public Ability getAbility() {
        return ability;
    }

    public void setAbility(Ability ability) {
        this.ability = ability;
    }

    public boolean isGranted() {
        return granted;
    }

    public void setGranted(boolean granted) {
        this.granted = granted;
    }

    public Long getGrantedBy() {
        return grantedBy;
    }

    public void setGrantedBy(Long grantedBy) {
        this.grantedBy = grantedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
