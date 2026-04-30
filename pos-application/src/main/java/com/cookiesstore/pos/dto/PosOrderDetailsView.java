package com.cookiesstore.pos.dto;

import java.util.List;

public record PosOrderDetailsView(
    Long orderId,
    String incrementId,
    String customerName,
    String statusLabel,
    String createdAt,
    String totalAmount,
    String currencyCode,
    int totalItems,
    List<PosOrderDetailItemView> items
) {
}
