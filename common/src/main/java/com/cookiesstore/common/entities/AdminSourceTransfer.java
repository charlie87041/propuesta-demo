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
    name = "admin_source_transfers",
    indexes = {
        @Index(name = "idx_admin_source_transfers_source_from_status", columnList = "source_id_from,status"),
        @Index(name = "idx_admin_source_transfers_source_to_status", columnList = "source_id_to,status")
    }
)
public class AdminSourceTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id_from", nullable = false)
    private Source sourceFrom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id_to", nullable = false)
    private Source sourceTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AdminSourceTransferStatus status = AdminSourceTransferStatus.DRAFT;

    @Column(name = "reference_code", length = 80)
    private String referenceCode;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "total_items", nullable = false)
    private Integer totalItems = 0;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity = 0;

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

    public Source getSourceFrom() {
        return sourceFrom;
    }

    public void setSourceFrom(Source sourceFrom) {
        this.sourceFrom = sourceFrom;
    }

    public Source getSourceTo() {
        return sourceTo;
    }

    public void setSourceTo(Source sourceTo) {
        this.sourceTo = sourceTo;
    }

    public AdminSourceTransferStatus getStatus() {
        return status;
    }

    public void setStatus(AdminSourceTransferStatus status) {
        this.status = status;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
