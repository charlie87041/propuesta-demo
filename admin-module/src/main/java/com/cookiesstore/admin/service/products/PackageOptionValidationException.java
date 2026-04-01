package com.cookiesstore.admin.service.products;

public class PackageOptionValidationException extends ProductDomainException {

    public PackageOptionValidationException(String messageKey) {
        super(messageKey);
    }
}
