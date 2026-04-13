package com.cookiesstore.admin.web.dto.products;

import com.cookiesstore.common.entities.ProductTemplateFieldType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductTemplateFieldForm {

    private Long templateId;

    @NotBlank
    @Size(max = 120)
    private String fieldKey;

    @NotBlank
    @Size(max = 120)
    private String label;

    @NotNull
    private ProductTemplateFieldType fieldType;

    private boolean required;
    private String defaultValue;
    private String validationRules;
    private byte sortOrder;
    private Long id;

    public ProductTemplateFieldForm() {
    }

    public ProductTemplateFieldForm(
        Long templateId,
        String fieldKey,
        String label,
        ProductTemplateFieldType fieldType,
        boolean required,
        String defaultValue,
        String validationRules,
        byte sortOrder,
        Long id
    ) {
        this.templateId = templateId;
        this.fieldKey = fieldKey;
        this.label = label;
        this.fieldType = fieldType;
        this.required = required;
        this.defaultValue = defaultValue;
        this.validationRules = validationRules;
        this.sortOrder = sortOrder;
        this.id = id;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public void setFieldKey(String fieldKey) {
        this.fieldKey = fieldKey;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ProductTemplateFieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(ProductTemplateFieldType fieldType) {
        this.fieldType = fieldType;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getValidationRules() {
        return validationRules;
    }

    public void setValidationRules(String validationRules) {
        this.validationRules = validationRules;
    }

    public byte getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(byte sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Record-style accessors kept for compatibility with existing service/controller code.
    public Long templateId() {
        return templateId;
    }

    public String fieldKey() {
        return fieldKey;
    }

    public String label() {
        return label;
    }

    public ProductTemplateFieldType fieldType() {
        return fieldType;
    }

    public boolean required() {
        return required;
    }

    public String defaultValue() {
        return defaultValue;
    }

    public String validationRules() {
        return validationRules;
    }

    public byte sortOrder() {
        return sortOrder;
    }

    public Long id() {
        return id;
    }
}
