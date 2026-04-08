package com.cookiesstore.admin.web.dto.products;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProductTemplateForm(
    @NotBlank @Size(max = 80) String code,
    @NotBlank @Size(max = 180) String name,
    @Size(max = 5000) String description,
    boolean active,
    @Valid List<ProductTemplateFieldForm> fields
) {
    public UpdateProductTemplateForm {
        fields = fields == null ? List.of() : fields;
    }
    
}
