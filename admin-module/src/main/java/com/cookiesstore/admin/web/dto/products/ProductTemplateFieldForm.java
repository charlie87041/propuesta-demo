package com.cookiesstore.admin.web.dto.products;


import com.cookiesstore.common.entities.ProductTemplateFieldType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductTemplateFieldForm(
    Long templateId,
    @NotBlank @Size(max = 120) String fieldKey,
    @NotBlank @Size(max = 120) String label,
    @NotNull ProductTemplateFieldType fieldType,
    boolean required,
    String defaultValue,
    String validationRules,
    byte sortOrder,
    Long id
) {
}
