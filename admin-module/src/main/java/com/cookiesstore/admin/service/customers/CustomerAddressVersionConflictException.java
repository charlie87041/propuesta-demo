package com.cookiesstore.admin.service.customers;

public class CustomerAddressVersionConflictException extends CustomerAddressDomainException {

    public CustomerAddressVersionConflictException(Long addressId) {
        super("admin.customers.addresses.error.notLatest");
    }
}

