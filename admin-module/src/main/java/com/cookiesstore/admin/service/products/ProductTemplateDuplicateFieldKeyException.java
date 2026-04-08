package com.cookiesstore.admin.service.products;

public class ProductTemplateDuplicateFieldKeyException extends ProductTemplateDomainException {

    public ProductTemplateDuplicateFieldKeyException(String fieldKey) {
        super("admin.product-templates.field.duplicateKey");
    }
}
