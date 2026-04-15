package com.cookiesstore.common.services.orders;

public class OrderItemsRequiredException extends OrderDomainException {

    public OrderItemsRequiredException() {
        super("Order must contain at least one item");
    }
}

