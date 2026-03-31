package com.cookiesstore.admin.web.dto.products;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateProductForm(
    @NotBlank @Size(max = 80) String sku,
    @NotBlank @Size(max = 180) String name,
    @NotBlank @Size(max = 200) String slug,
    @Size(max = 5000) String description,
    @NotNull Long categoryId,
    @Size(max = 255) String mainImageUrl,
    @Size(max = 5000) String ingredients,
    @Size(max = 5000) String allergenInfo,
    @Size(max = 5000) String nutritionFacts,
    boolean active,
    boolean visible
) {
}
