package com.cookiesstore.admin.web.dto.sources;

import java.math.BigDecimal;

import com.cookiesstore.common.entities.AdminSourcePurchaseOrderItemType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class PurchaseOrderItemForm {

    @NotNull
    private AdminSourcePurchaseOrderItemType itemType = AdminSourcePurchaseOrderItemType.CATALOG_PRODUCT;

    private Long productId;

    @Size(max = 180)
    private String productName;

    @Size(max = 80)
    private String sku;

    @NotNull
    @Positive
    private Integer orderedQty;

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal unitCost = BigDecimal.ZERO;

    private String note;

    public PurchaseOrderItemForm() {
    }

    public AdminSourcePurchaseOrderItemType getItemType() {
        return itemType;
    }

    public void setItemType(AdminSourcePurchaseOrderItemType itemType) {
        this.itemType = itemType;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getOrderedQty() {
        return orderedQty;
    }

    public void setOrderedQty(Integer orderedQty) {
        this.orderedQty = orderedQty;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
