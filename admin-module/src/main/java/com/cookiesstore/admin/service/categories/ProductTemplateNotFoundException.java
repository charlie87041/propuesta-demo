package com.cookiesstore.admin.service.categories;

public class ProductTemplateNotFoundException extends CategoryDomainException {

    public ProductTemplateNotFoundException(Long templateId) {
        super("admin.categories.error.productTemplate.notFound");
    }
}
