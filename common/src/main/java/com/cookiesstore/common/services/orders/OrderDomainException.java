package com.cookiesstore.common.services.orders;

public abstract class OrderDomainException extends IllegalArgumentException {

    protected OrderDomainException(String message) {
        super(message);
    }
}

