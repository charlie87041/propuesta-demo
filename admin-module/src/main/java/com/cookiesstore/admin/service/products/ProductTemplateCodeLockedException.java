package com.cookiesstore.admin.service.products;

public class ProductTemplateCodeLockedException extends ProductTemplateDomainException {

    public ProductTemplateCodeLockedException(String code) {
        super("admin.product-templates.error.code.locked");
    }
}
