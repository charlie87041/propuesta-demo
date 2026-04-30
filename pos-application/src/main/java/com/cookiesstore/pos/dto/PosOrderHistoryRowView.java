package com.cookiesstore.pos.dto;

public record PosOrderHistoryRowView(
    Long id,
    String incrementId,
    String timestamp,
    String customerName,
    String itemsLabel,
    String totalAmount,
    String statusLabel,
    String statusTone
) {
}
