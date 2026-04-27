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
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(
    name = "admin_source_transfer_incidents",
    indexes = {
        @Index(name = "idx_admin_source_transfer_incidents_transfer_created", columnList = "transfer_id,created_at")
    }
)
public class AdminSourceTransferIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transfer_id", nullable = false)
    private AdminSourceTransfer transfer;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_type", nullable = false, length = 20)
    private AdminSourceTransferIncidentType incidentType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "created_by_admin_user_id")
    private Long createdByAdminUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public AdminSourceTransfer getTransfer() {
        return transfer;
    }

    public void setTransfer(AdminSourceTransfer transfer) {
        this.transfer = transfer;
    }

    public AdminSourceTransferIncidentType getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(AdminSourceTransferIncidentType incidentType) {
        this.incidentType = incidentType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCreatedByAdminUserId() {
        return createdByAdminUserId;
    }

    public void setCreatedByAdminUserId(Long createdByAdminUserId) {
        this.createdByAdminUserId = createdByAdminUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
