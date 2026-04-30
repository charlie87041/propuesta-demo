package com.cookiesstore.pos.dto;

public record PosCatalogItemView(
    Long id,
    String name,
    String type,
    double price,
    long unitPriceMinor,
    int sourceStock,
    int lowStockThreshold
) {
}
