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
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(
    name = "admin_source_purchase_order_status_history",
    indexes = {
        @Index(
            name = "idx_admin_source_purchase_order_status_history_order_changed",
            columnList = "purchase_order_id,changed_at"
        )
    }
)
public class AdminSourcePurchaseOrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private AdminSourcePurchaseOrder purchaseOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private AdminSourcePurchaseOrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 30)
    private AdminSourcePurchaseOrderStatus toStatus;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @Column(name = "changed_by_admin_user_id")
    private Long changedByAdminUserId;

    @Column(columnDefinition = "TEXT")
    private String note;

    public Long getId() {
        return id;
    }

    public AdminSourcePurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(AdminSourcePurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public AdminSourcePurchaseOrderStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(AdminSourcePurchaseOrderStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public AdminSourcePurchaseOrderStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(AdminSourcePurchaseOrderStatus toStatus) {
        this.toStatus = toStatus;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }

    public Long getChangedByAdminUserId() {
        return changedByAdminUserId;
    }

    public void setChangedByAdminUserId(Long changedByAdminUserId) {
        this.changedByAdminUserId = changedByAdminUserId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
