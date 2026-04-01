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
    name = "product_components",
    indexes = {
        @Index(name = "idx_product_components_parent_sort", columnList = "parent_product_id,sort_order"),
        @Index(name = "idx_product_components_child", columnList = "child_product_id"),
        @Index(name = "idx_product_components_source", columnList = "source_id")
    }
)
public class ProductComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_product_id", nullable = false)
    private Product parentProduct;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "child_product_id", nullable = false)
    private Product childProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private Source source;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity = BigDecimal.ONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_price_mode", nullable = false, length = 30)
    private ProductComponentPriceMode unitPriceMode = ProductComponentPriceMode.INHERIT_PRODUCT_PRICE;

    @Column(name = "unit_price_override", precision = 12, scale = 2)
    private BigDecimal unitPriceOverride;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

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

    public Product getParentProduct() {
        return parentProduct;
    }

    public void setParentProduct(Product parentProduct) {
        this.parentProduct = parentProduct;
    }

    public Product getChildProduct() {
        return childProduct;
    }

    public void setChildProduct(Product childProduct) {
        this.childProduct = childProduct;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public ProductComponentPriceMode getUnitPriceMode() {
        return unitPriceMode;
    }

    public void setUnitPriceMode(ProductComponentPriceMode unitPriceMode) {
        this.unitPriceMode = unitPriceMode;
    }

    public BigDecimal getUnitPriceOverride() {
        return unitPriceOverride;
    }

    public void setUnitPriceOverride(BigDecimal unitPriceOverride) {
        this.unitPriceOverride = unitPriceOverride;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
