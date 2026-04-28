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

@Entity
@Table(
    name = "admin_source_purchase_order_items",
    indexes = {
        @Index(name = "idx_admin_source_purchase_order_items_purchase_order", columnList = "purchase_order_id"),
        @Index(name = "idx_admin_source_purchase_order_items_product", columnList = "product_id")
    }
)
public class AdminSourcePurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private AdminSourcePurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 30)
    private AdminSourcePurchaseOrderItemType itemType = AdminSourcePurchaseOrderItemType.CATALOG_PRODUCT;

    @Column(length = 80)
    private String sku;

    @Column(name = "product_name", nullable = false, length = 180)
    private String productName;

    @Column(name = "ordered_qty", nullable = false)
    private Integer orderedQty;

    @Column(name = "received_qty", nullable = false)
    private Integer receivedQty = 0;

    @Column(name = "unit_cost_minor", nullable = false)
    private Long unitCostMinor = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_code", referencedColumnName = "code")
    private Currency currency;

    @Column(name = "line_total_minor", nullable = false)
    private Long lineTotalMinor = 0L;

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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public AdminSourcePurchaseOrderItemType getItemType() {
        return itemType;
    }

    public void setItemType(AdminSourcePurchaseOrderItemType itemType) {
        this.itemType = itemType;
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

    public Integer getOrderedQty() {
        return orderedQty;
    }

    public void setOrderedQty(Integer orderedQty) {
        this.orderedQty = orderedQty;
    }

    public Integer getReceivedQty() {
        return receivedQty;
    }

    public void setReceivedQty(Integer receivedQty) {
        this.receivedQty = receivedQty;
    }

    public Long getUnitCostMinor() {
        return unitCostMinor;
    }

    public void setUnitCostMinor(Long unitCostMinor) {
        this.unitCostMinor = unitCostMinor;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Long getLineTotalMinor() {
        return lineTotalMinor;
    }

    public void setLineTotalMinor(Long lineTotalMinor) {
        this.lineTotalMinor = lineTotalMinor;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
