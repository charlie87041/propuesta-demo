package com.cookiesstore.admin.service.products;

public class ProductNotFoundException extends ProductDomainException {

    public ProductNotFoundException(Long productId) {
        super("admin.products.flash.notFound");
    }
}
