package com.cookiesstore.admin.web.dto.products;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record PackageComponentForm(
    @NotNull Long packageProductId,
    @NotNull Long optionTypeId,
    Long categoryId,
    @NotNull String name,
    Integer minSelect,
    Integer maxSelect,
    List<PackageComponentItemForm> items,
    int sortOrder
) {
}
