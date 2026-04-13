package com.cookiesstore.admin.service.settings.currencies;

public abstract class CurrencyDomainException extends RuntimeException {

    private final String messageKey;

    protected CurrencyDomainException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}

