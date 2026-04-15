package com.cookiesstore.common.services.orders;

public class OrderNotFoundException extends OrderDomainException {

    public OrderNotFoundException(Long orderId) {
        super("Order not found: " + orderId);
    }
}

