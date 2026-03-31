package com.cookiesstore.admin.service.products;

public class ProductSlugExistsException extends ProductDomainException {

    public ProductSlugExistsException(String slug) {
        super("admin.products.error.slug.exists");
    }
}
