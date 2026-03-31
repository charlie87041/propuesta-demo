package com.cookiesstore.admin.service.products;

public class ProductUniqueConstraintException extends ProductDomainException {

    public ProductUniqueConstraintException() {
        super("admin.products.error.unique");
    }
}
