package com.cookiesstore.pos.dto;

public record PosOrderDetailItemView(
    String productName,
    String productDescription,
    Integer quantity,
    String unitPrice,
    String lineTotal
) {
}

