package com.cookiesstore.admin.service.products;

public class ProductPriceValidationException extends ProductDomainException {

    public ProductPriceValidationException(String messageKey) {
        super(messageKey);
    }
}
