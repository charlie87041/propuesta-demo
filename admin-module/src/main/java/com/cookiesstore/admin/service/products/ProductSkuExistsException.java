package com.cookiesstore.admin.service.products;

public class ProductSkuExistsException extends ProductDomainException {

    public ProductSkuExistsException(String sku) {
        super("admin.products.error.sku.exists");
    }
}
