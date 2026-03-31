package com.cookiesstore.admin.service.products;

public class ProductCategoryNotFoundException extends ProductDomainException {

    public ProductCategoryNotFoundException(Long categoryId) {
        super("admin.products.error.category.notFound");
    }
}
