package com.cookiesstore.pos.services;

public class PosSessionDomainException extends RuntimeException {

    private final String messageKey;

    public PosSessionDomainException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
