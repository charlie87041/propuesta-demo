package com.cookiesstore.common.services.orders;

public class OrderProductNotFoundException extends OrderDomainException {

    public OrderProductNotFoundException(Long productId) {
        super("Product not found for order: " + productId);
    }
}

