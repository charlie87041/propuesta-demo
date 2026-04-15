package com.cookiesstore.common.services.orders;

public class OrderItemNotFoundException extends OrderDomainException {

    public OrderItemNotFoundException(Long orderItemId) {
        super("Order item not found: " + orderItemId);
    }
}

