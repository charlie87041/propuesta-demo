package com.cookiesstore.admin.web.dto.products;

public record VariantProductForm(
    Long variantProductId,
    Long parentProduct,
    Boolean defaultVariant,
    Integer sortOrder
) {
    public VariantProductForm {
        defaultVariant = defaultVariant != null && defaultVariant;
        sortOrder = sortOrder == null ? 0 : sortOrder;
    }
}
