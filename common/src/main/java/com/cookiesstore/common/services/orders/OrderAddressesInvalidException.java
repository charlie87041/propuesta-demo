package com.cookiesstore.common.services.orders;

public class OrderAddressesInvalidException extends OrderDomainException {

    public OrderAddressesInvalidException(int actualCount) {
        super("Order must include exactly 2 addresses (SHIPPING and BILLING). Actual: " + actualCount);
    }
}

