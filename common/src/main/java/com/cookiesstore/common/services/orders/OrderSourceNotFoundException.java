package com.cookiesstore.common.services.orders;

public class OrderSourceNotFoundException extends OrderDomainException {

    public OrderSourceNotFoundException(Long productId, Long sourceId) {
        super("Source " + sourceId + " not available for product " + productId);
    }
}

