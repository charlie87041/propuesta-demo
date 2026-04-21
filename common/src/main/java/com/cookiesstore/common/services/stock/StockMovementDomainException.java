package com.cookiesstore.common.services.stock;

public abstract class StockMovementDomainException extends IllegalArgumentException {

    protected StockMovementDomainException(String message) {
        super(message);
    }
}
