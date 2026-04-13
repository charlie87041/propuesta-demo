package com.cookiesstore.admin.web.dto.products;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class UpdateProductTemplateForm {

    @NotBlank
    @Size(max = 80)
    private String code;

    @NotBlank
    @Size(max = 180)
    private String name;

    @Size(max = 5000)
    private String description;

    private boolean active;

    @Valid
    private List<ProductTemplateFieldForm> fields = new ArrayList<>();

    public UpdateProductTemplateForm() {
    }

    public UpdateProductTemplateForm(
        String code,
        String name,
        String description,
        boolean active,
        List<ProductTemplateFieldForm> fields
    ) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.active = active;
        this.fields = fields == null ? new ArrayList<>() : new ArrayList<>(fields);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<ProductTemplateFieldForm> getFields() {
        return fields;
    }

    public void setFields(List<ProductTemplateFieldForm> fields) {
        this.fields = fields == null ? new ArrayList<>() : fields;
    }

    // Record-style accessors kept for compatibility with existing service/controller code.
    public String code() {
        return code;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public boolean active() {
        return active;
    }

    public List<ProductTemplateFieldForm> fields() {
        return fields;
    }
}
