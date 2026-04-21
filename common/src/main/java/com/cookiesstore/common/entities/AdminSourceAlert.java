package com.cookiesstore.common.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    name = "admin_source_alerts",
    indexes = {
        @Index(name = "idx_admin_source_alerts_source_status", columnList = "source_id,status"),
        @Index(name = "idx_admin_source_alerts_product_status", columnList = "product_id,status"),
        @Index(name = "idx_admin_source_alerts_triggered_at", columnList = "triggered_at")
    }
)
public class AdminSourceAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 30)
    private AdminSourceAlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdminSourceAlertStatus status = AdminSourceAlertStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdminSourceAlertSeverity severity = AdminSourceAlertSeverity.MEDIUM;

    @Column(name = "threshold_value")
    private Integer thresholdValue;

    @Column(name = "current_value")
    private Integer currentValue;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "triggered_at", nullable = false)
    private Instant triggeredAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "resolved_by_admin_user_id")
    private Long resolvedByAdminUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.triggeredAt = this.triggeredAt == null ? now : this.triggeredAt;
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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public AdminSourceAlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AdminSourceAlertType alertType) {
        this.alertType = alertType;
    }

    public AdminSourceAlertStatus getStatus() {
        return status;
    }

    public void setStatus(AdminSourceAlertStatus status) {
        this.status = status;
    }

    public AdminSourceAlertSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AdminSourceAlertSeverity severity) {
        this.severity = severity;
    }

    public Integer getThresholdValue() {
        return thresholdValue;
    }

    public void setThresholdValue(Integer thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    public Integer getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Integer currentValue) {
        this.currentValue = currentValue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTriggeredAt() {
        return triggeredAt;
    }

    public void setTriggeredAt(Instant triggeredAt) {
        this.triggeredAt = triggeredAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Long getResolvedByAdminUserId() {
        return resolvedByAdminUserId;
    }

    public void setResolvedByAdminUserId(Long resolvedByAdminUserId) {
        this.resolvedByAdminUserId = resolvedByAdminUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
