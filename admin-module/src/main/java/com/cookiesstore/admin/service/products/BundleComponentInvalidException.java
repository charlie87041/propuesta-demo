package com.cookiesstore.admin.service.products;

public class BundleComponentInvalidException extends ProductDomainException {

    public BundleComponentInvalidException(String messageKey) {
        super(messageKey);
    }
}
