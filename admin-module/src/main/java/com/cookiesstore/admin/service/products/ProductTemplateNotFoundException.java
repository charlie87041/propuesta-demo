package com.cookiesstore.admin.service.products;

public class ProductTemplateNotFoundException extends ProductTemplateDomainException {

    public ProductTemplateNotFoundException(Long templateId) {
        super("admin.product-templates.flash.notFound");
    }
}
