package com.cookiesstore.admin.service.products;

public class ProductTemplateFieldInvalidReferenceException extends ProductTemplateDomainException {

    public ProductTemplateFieldInvalidReferenceException(Long fieldId, Long templateId) {
        super("admin.product-templates.field.invalidReference");
    }
}
