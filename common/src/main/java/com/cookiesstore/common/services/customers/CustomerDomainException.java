package com.cookiesstore.common.services.customers;

public abstract class CustomerDomainException extends IllegalArgumentException {

    private final String messageKey;

    protected CustomerDomainException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
