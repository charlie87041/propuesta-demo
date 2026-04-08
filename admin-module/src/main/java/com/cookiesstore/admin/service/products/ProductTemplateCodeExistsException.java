package com.cookiesstore.admin.service.products;

public class ProductTemplateCodeExistsException extends ProductTemplateDomainException {

    public ProductTemplateCodeExistsException(String code) {
        super("admin.product-templates.error.code.exists");
    }
}
