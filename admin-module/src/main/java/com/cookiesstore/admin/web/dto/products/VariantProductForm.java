package com.cookiesstore.admin.web.dto.products;

public record VariantProductForm(
    Long variantProductId,
    Long parentProduct,
    boolean defaultVariant,
    int sortOrder
) {
}
