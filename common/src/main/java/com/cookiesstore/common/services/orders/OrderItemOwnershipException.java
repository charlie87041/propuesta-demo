package com.cookiesstore.common.services.orders;

public class OrderItemOwnershipException extends OrderDomainException {

    public OrderItemOwnershipException(Long orderId, Long orderItemId) {
        super("Order item " + orderItemId + " does not belong to order " + orderId);
    }
}

