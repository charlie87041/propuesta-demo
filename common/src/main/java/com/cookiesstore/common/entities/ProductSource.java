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
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "product_sources",
    indexes = {
        @Index(name = "uk_product_source", columnList = "product_id,source_id", unique = true),
        @Index(name = "idx_product_sources_source", columnList = "source_id"),
        @Index(name = "idx_product_sources_status", columnList = "status")
    }
)
public class ProductSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity = 0;

    @Column(name = "low_stock_threshold", nullable = false)
    private int lowStockThreshold = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductSourceStatus status = ProductSourceStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_id")
    private Price price;

    @Column(name = "last_purchase_cost_minor")
    private Long lastPurchaseCostMinor;

    @Column(name = "average_purchase_cost_minor")
    private Long averagePurchaseCostMinor;

    @Column(name = "last_purchase_at")
    private Instant lastPurchaseAt;

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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public ProductSourceStatus getStatus() {
        return status;
    }

    public void setStatus(ProductSourceStatus status) {
        this.status = status;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public Long getLastPurchaseCostMinor() {
        return lastPurchaseCostMinor;
    }

    public void setLastPurchaseCostMinor(Long lastPurchaseCostMinor) {
        this.lastPurchaseCostMinor = lastPurchaseCostMinor;
    }

    public BigDecimal getLastPurchaseCostAmount() {
        return toAmount(lastPurchaseCostMinor);
    }

    public Long getAveragePurchaseCostMinor() {
        return averagePurchaseCostMinor;
    }

    public void setAveragePurchaseCostMinor(Long averagePurchaseCostMinor) {
        this.averagePurchaseCostMinor = averagePurchaseCostMinor;
    }

    public BigDecimal getAveragePurchaseCostAmount() {
        return toAmount(averagePurchaseCostMinor);
    }

    public Instant getLastPurchaseAt() {
        return lastPurchaseAt;
    }

    public void setLastPurchaseAt(Instant lastPurchaseAt) {
        this.lastPurchaseAt = lastPurchaseAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private BigDecimal toAmount(Long minor) {
        if (minor == null) {
            return null;
        }
        return BigDecimal.valueOf(minor, 2);
    }
}
