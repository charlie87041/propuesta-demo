package com.cookiesstore.pos.services;

public class PosOrderDomainException extends RuntimeException {

    private final String messageKey;

    public PosOrderDomainException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
