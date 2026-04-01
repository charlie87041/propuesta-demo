package com.cookiesstore.admin.service.products;

public class VariantProductValidationException extends ProductDomainException {

    public VariantProductValidationException(String messageKey) {
        super(messageKey);
    }
}
