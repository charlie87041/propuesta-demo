package com.cookiesstore.admin.service.products;

public abstract class ProductTemplateDomainException extends RuntimeException {

    private final String messageKey;

    protected ProductTemplateDomainException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
