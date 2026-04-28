package com.cookiesstore.admin.web.dto.sources;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ResolvePurchaseOrderAdhocItemForm {

    @NotNull
    private Long purchaseOrderItemId;

    @NotBlank
    @Size(max = 80)
    private String sku;

    @NotBlank
    @Size(max = 180)
    private String name;

    @NotBlank
    @Size(max = 200)
    private String slug;

    @NotNull
    private Long categoryId;

    @Size(max = 5000)
    private String description;

    @Size(max = 255)
    private String mainImageUrl;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @Digits(integer = 12, fraction = 2)
    private BigDecimal unitPrice = BigDecimal.valueOf(0.01);

    private boolean active = true;

    private boolean visible = true;

    private boolean listable = true;

    private boolean purchasable = true;

    private boolean purchasableAlone = true;

    private String originalProductName;

    private Integer orderedQty;

    private BigDecimal purchaseUnitCost;

    public ResolvePurchaseOrderAdhocItemForm() {
    }

    public Long getPurchaseOrderItemId() {
        return purchaseOrderItemId;
    }

    public void setPurchaseOrderItemId(Long purchaseOrderItemId) {
        this.purchaseOrderItemId = purchaseOrderItemId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMainImageUrl() {
        return mainImageUrl;
    }

    public void setMainImageUrl(String mainImageUrl) {
        this.mainImageUrl = mainImageUrl;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isListable() {
        return listable;
    }

    public void setListable(boolean listable) {
        this.listable = listable;
    }

    public boolean isPurchasable() {
        return purchasable;
    }

    public void setPurchasable(boolean purchasable) {
        this.purchasable = purchasable;
    }

    public boolean isPurchasableAlone() {
        return purchasableAlone;
    }

    public void setPurchasableAlone(boolean purchasableAlone) {
        this.purchasableAlone = purchasableAlone;
    }

    public String getOriginalProductName() {
        return originalProductName;
    }

    public void setOriginalProductName(String originalProductName) {
        this.originalProductName = originalProductName;
    }

    public Integer getOrderedQty() {
        return orderedQty;
    }

    public void setOrderedQty(Integer orderedQty) {
        this.orderedQty = orderedQty;
    }

    public BigDecimal getPurchaseUnitCost() {
        return purchaseUnitCost;
    }

    public void setPurchaseUnitCost(BigDecimal purchaseUnitCost) {
        this.purchaseUnitCost = purchaseUnitCost;
    }
}
