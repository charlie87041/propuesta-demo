package com.cookiesstore.admin.web.dto.sources;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class TransferIncidentItemForm {

    @NotNull
    private Long productId;

    @NotNull
    @Min(0)
    private Integer missingQuantity;

    public TransferIncidentItemForm() {
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getMissingQuantity() {
        return missingQuantity;
    }

    public void setMissingQuantity(Integer missingQuantity) {
        this.missingQuantity = missingQuantity;
    }
}
