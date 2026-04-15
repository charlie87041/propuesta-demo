package com.cookiesstore.common.dto.orders;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemForm(
    Long orderItemId,
    @NotNull Long productId,
    @NotNull Long sourceId,
    @Min(value = 1) Integer totalQtyOrdered,
    String couponCode
){}