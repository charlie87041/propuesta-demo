package com.cookiesstore.common.services.orders;

public class OrderPriceNotFoundException extends OrderDomainException {

    public OrderPriceNotFoundException(Long productId, Long sourceId) {
        super("Price not found for product " + productId + " and source " + sourceId);
    }
}

