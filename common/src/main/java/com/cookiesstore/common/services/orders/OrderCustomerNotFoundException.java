package com.cookiesstore.common.services.orders;

public class OrderCustomerNotFoundException extends OrderDomainException {

    public OrderCustomerNotFoundException(Long customerId) {
        super("Customer not found for order: " + customerId);
    }
}

