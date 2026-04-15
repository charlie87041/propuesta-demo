package com.cookiesstore.common.services.orders;

public class OrderCurrencyNotFoundException extends OrderDomainException {

    public OrderCurrencyNotFoundException(String currencyCode) {
        super("Currency not found for order: " + currencyCode);
    }
}

