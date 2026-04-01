package com.cookiesstore.admin.web.dto.products;

import jakarta.validation.constraints.NotNull;

public record PackageComponentItemForm(
    Long itemId,
    @NotNull Long productId,
    Long sourceId,
    int sortOrder,
    boolean isDefault,
    @NotNull String extraPriceMode,
    Double extraPrice
) {
}
