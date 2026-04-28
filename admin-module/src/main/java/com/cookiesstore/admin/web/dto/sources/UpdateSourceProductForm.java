package com.cookiesstore.admin.web.dto.sources;

import java.math.BigDecimal;

import com.cookiesstore.common.entities.ProductSourceStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateSourceProductForm {

    @DecimalMin(value = "0.00", message = "admin.products.error.source.price.invalid")
    private BigDecimal sourcePrice;

    @NotNull
    @Min(value = 0)
    private Integer stockQuantity = 0;

    @NotNull
    @Min(value = 0)
    private Integer lowStockThreshold = 0;

    @NotNull
    private ProductSourceStatus sourceStatus = ProductSourceStatus.ACTIVE;

    public BigDecimal getSourcePrice() {
        return sourcePrice;
    }

    public void setSourcePrice(BigDecimal sourcePrice) {
        this.sourcePrice = sourcePrice;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public ProductSourceStatus getSourceStatus() {
        return sourceStatus;
    }

    public void setSourceStatus(ProductSourceStatus sourceStatus) {
        this.sourceStatus = sourceStatus;
    }
}
