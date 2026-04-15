package com.cookiesstore.common.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    name = "order_items",
    indexes = {
        @Index(name = "idx_order_items_order_id", columnList = "order_id"),
        @Index(name = "idx_order_items_parent_id", columnList = "parent_id"),
        @Index(name = "idx_order_items_product_id", columnList = "product_id")
    }
)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private OrderItem parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(length = 80)
    private String sku;

    @Column(name = "product_name", nullable = false, length = 180)
    private String productName;

    @Column(name = "product_type_code", length = 30)
    private String productTypeCode;

    @Column(name = "quantity_ordered", nullable = false)
    private Integer quantityOrdered = 0;

    @Column(name = "quantity_shipped", nullable = false)
    private Integer quantityShipped = 0;

    @Column(name = "quantity_invoiced", nullable = false)
    private Integer quantityInvoiced = 0;

    @Column(name = "quantity_canceled", nullable = false)
    private Integer quantityCanceled = 0;

    @Column(name = "quantity_refunded", nullable = false)
    private Integer quantityRefunded = 0;

    @Column(name = "unit_price_minor", nullable = false)
    private Long unitPriceMinor = 0L;

    @Column(name = "line_sub_total_minor", nullable = false)
    private Long lineSubTotalMinor = 0L;

    @Column(name = "line_discount_minor", nullable = false)
    private Long lineDiscountMinor = 0L;

    @Column(name = "line_tax_minor", nullable = false)
    private Long lineTaxMinor = 0L;

    @Column(name = "line_total_minor", nullable = false)
    private Long lineTotalMinor = 0L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency_code", nullable = false)
    private Currency currency;

    @Column(name = "additional", columnDefinition = "jsonb")
    private String additional;

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

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public OrderItem getParent() {
        return parent;
    }

    public void setParent(OrderItem parent) {
        this.parent = parent;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductTypeCode() {
        return productTypeCode;
    }

    public void setProductTypeCode(String productTypeCode) {
        this.productTypeCode = productTypeCode;
    }

    public Integer getQuantityOrdered() {
        return quantityOrdered;
    }

    public void setQuantityOrdered(Integer quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
    }

    public Integer getQuantityShipped() {
        return quantityShipped;
    }

    public void setQuantityShipped(Integer quantityShipped) {
        this.quantityShipped = quantityShipped;
    }

    public Integer getQuantityInvoiced() {
        return quantityInvoiced;
    }

    public void setQuantityInvoiced(Integer quantityInvoiced) {
        this.quantityInvoiced = quantityInvoiced;
    }

    public Integer getQuantityCanceled() {
        return quantityCanceled;
    }

    public void setQuantityCanceled(Integer quantityCanceled) {
        this.quantityCanceled = quantityCanceled;
    }

    public Integer getQuantityRefunded() {
        return quantityRefunded;
    }

    public void setQuantityRefunded(Integer quantityRefunded) {
        this.quantityRefunded = quantityRefunded;
    }

    public Long getUnitPriceMinor() {
        return unitPriceMinor;
    }

    public void setUnitPriceMinor(Long unitPriceMinor) {
        this.unitPriceMinor = unitPriceMinor;
    }

    public Long getLineSubTotalMinor() {
        return lineSubTotalMinor;
    }

    public void setLineSubTotalMinor(Long lineSubTotalMinor) {
        this.lineSubTotalMinor = lineSubTotalMinor;
    }

    public Long getLineDiscountMinor() {
        return lineDiscountMinor;
    }

    public void setLineDiscountMinor(Long lineDiscountMinor) {
        this.lineDiscountMinor = lineDiscountMinor;
    }

    public Long getLineTaxMinor() {
        return lineTaxMinor;
    }

    public void setLineTaxMinor(Long lineTaxMinor) {
        this.lineTaxMinor = lineTaxMinor;
    }

    public Long getLineTotalMinor() {
        return lineTotalMinor;
    }

    public void setLineTotalMinor(Long lineTotalMinor) {
        this.lineTotalMinor = lineTotalMinor;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

