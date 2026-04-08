package com.cookiesstore.admin.service.products;

public class ProductTemplateFieldNotFoundException extends ProductTemplateDomainException {

    public ProductTemplateFieldNotFoundException(Long fieldId) {
        super("admin.product-templates.field.notFound");
    }
}
