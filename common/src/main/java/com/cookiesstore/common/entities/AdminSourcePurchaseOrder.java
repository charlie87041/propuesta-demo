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
    name = "admin_source_purchase_orders",
    indexes = {
        @Index(name = "idx_admin_source_purchase_orders_source_status", columnList = "source_id,status"),
        @Index(name = "idx_admin_source_purchase_orders_expected_at", columnList = "expected_at")
    }
)
public class AdminSourcePurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    @Column(name = "supplier_name", nullable = false, length = 180)
    private String supplierName;

    @Column(name = "supplier_reference", length = 100)
    private String supplierReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AdminSourcePurchaseOrderStatus status = AdminSourcePurchaseOrderStatus.DRAFT;

    @Column(name = "expected_at")
    private Instant expectedAt;

    @Column(name = "placed_at")
    private Instant placedAt;

    @Column(name = "received_at")
    private Instant receivedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "total_items", nullable = false)
    private Integer totalItems = 0;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity = 0;

    @Column(name = "created_by_admin_user_id")
    private Long createdByAdminUserId;

    @Column(name = "updated_by_admin_user_id")
    private Long updatedByAdminUserId;

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

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierReference() {
        return supplierReference;
    }

    public void setSupplierReference(String supplierReference) {
        this.supplierReference = supplierReference;
    }

    public AdminSourcePurchaseOrderStatus getStatus() {
        return status;
    }

    public void setStatus(AdminSourcePurchaseOrderStatus status) {
        this.status = status;
    }

    public String getCurrentStatusName() {
        return this.status == null ? null : this.status.name();
    }

    public Instant getExpectedAt() {
        return expectedAt;
    }

    public void setExpectedAt(Instant expectedAt) {
        this.expectedAt = expectedAt;
    }

    public Instant getPlacedAt() {
        return placedAt;
    }

    public void setPlacedAt(Instant placedAt) {
        this.placedAt = placedAt;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
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

    public Long getCreatedByAdminUserId() {
        return createdByAdminUserId;
    }

    public void setCreatedByAdminUserId(Long createdByAdminUserId) {
        this.createdByAdminUserId = createdByAdminUserId;
    }

    public Long getUpdatedByAdminUserId() {
        return updatedByAdminUserId;
    }

    public void setUpdatedByAdminUserId(Long updatedByAdminUserId) {
        this.updatedByAdminUserId = updatedByAdminUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
