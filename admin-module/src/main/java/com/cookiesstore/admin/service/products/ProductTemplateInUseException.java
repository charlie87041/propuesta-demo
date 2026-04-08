package com.cookiesstore.admin.service.products;

public class ProductTemplateInUseException extends ProductTemplateDomainException {

    public ProductTemplateInUseException(Long productTemplateId) {
        super("admin.product-templates.inUse");
    }
}
