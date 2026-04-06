package com.cookiesstore.admin.web.dto.categories;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCategoryForm(
    @NotBlank @Size(max = 80) String code,
    @NotBlank @Size(max = 120) String name,
    @NotBlank @Size(max = 140) String slug,
    @Size(max = 1000) String description,
    @Min(0) @Max(9999) int sortOrder,
    boolean active,
    @NotNull Long productTemplateId
) {
}
