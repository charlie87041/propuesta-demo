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
    name = "admin_source_stock_movements",
    indexes = {
        @Index(name = "idx_admin_source_stock_movements_source_created_at", columnList = "source_id,created_at"),
        @Index(name = "idx_admin_source_stock_movements_product_created_at", columnList = "product_id,created_at"),
        @Index(name = "idx_admin_source_stock_movements_type", columnList = "movement_type")
    }
)
public class AdminSourceStockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id")
    private AdminSourceTransfer transfer;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 40)
    private AdminSourceStockMovementType movementType;

    @Column(name = "quantity_delta", nullable = false)
    private Integer quantityDelta;

    @Column(name = "balance_after")
    private Integer balanceAfter;

    @Column(name = "reference_type", length = 40)
    private String referenceType;

    @Column(name = "reference_code", length = 80)
    private String referenceCode;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(columnDefinition = "TEXT")
    private String metadata;

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

    public AdminSourceStockMovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(AdminSourceStockMovementType movementType) {
        this.movementType = movementType;
    }

    public Integer getQuantityDelta() {
        return quantityDelta;
    }

    public void setQuantityDelta(Integer quantityDelta) {
        this.quantityDelta = quantityDelta;
    }

    public Integer getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(Integer balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
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

    public Order getOrder()
    {
        return this.order;
    }

    public void setOrder(Order order)
    {
        this.order = order;
    }

    public AdminSourceTransfer getTransfer() {
        return transfer;
    }

    public void setTransfer(AdminSourceTransfer transfer) {
        this.transfer = transfer;
    }
}
