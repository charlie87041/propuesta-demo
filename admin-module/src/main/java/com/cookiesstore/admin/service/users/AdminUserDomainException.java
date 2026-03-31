package com.cookiesstore.admin.service.users;

public abstract class AdminUserDomainException extends IllegalArgumentException {

    private final String messageKey;

    protected AdminUserDomainException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
