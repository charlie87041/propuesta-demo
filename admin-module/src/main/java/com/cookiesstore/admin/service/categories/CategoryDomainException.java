package com.cookiesstore.admin.service.categories;

public abstract class CategoryDomainException extends RuntimeException {

    private final String messageKey;

    protected CategoryDomainException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
