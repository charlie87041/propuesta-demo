package com.cookiesstore.admin.service.products;

public abstract class ProductDomainException extends RuntimeException {

    private final String messageKey;

    protected ProductDomainException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
