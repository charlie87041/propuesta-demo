package com.cookiesstore.pos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateOrderLineForm {

    @NotNull
    private Long productId;

    @NotBlank
    private String productName;

    @Min(1)
    private int quantity;

    @NotNull
    private Long unitPriceMinor;

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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Long getUnitPriceMinor() {
        return unitPriceMinor;
    }

    public void setUnitPriceMinor(Long unitPriceMinor) {
        this.unitPriceMinor = unitPriceMinor;
    }
}
