package com.cookiesstore.admin.web.dto.sources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProductSourceForm(
    @NotBlank @Size(max = 80) String code,
    @NotBlank @Size(max = 120) String name,
    @Size(max = 1000) String description,
    boolean active
) {
}