package com.cookiesstore.admin.service.products;

public class ProductTemplateValidationException extends ProductDomainException {

    public ProductTemplateValidationException(String fieldLabel) {
        super("admin.products.template.required-field");
    }
}
