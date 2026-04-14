package com.cookiesstore.admin.service.customers;

public abstract class CustomerAddressDomainException extends RuntimeException {

    private final String messageKey;

    protected CustomerAddressDomainException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}

