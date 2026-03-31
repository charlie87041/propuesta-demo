package com.cookiesstore.admin.service.sources;

public abstract class SourceDomainException extends RuntimeException {

    private final String messageKey;

    protected SourceDomainException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
