package com.cookiesstore.common.services.orders;

public class OrderItemQuantityInvalidException extends OrderDomainException {

    public OrderItemQuantityInvalidException(Long productId) {
        super("Order item quantity must be greater than zero for product " + productId);
    }
}

