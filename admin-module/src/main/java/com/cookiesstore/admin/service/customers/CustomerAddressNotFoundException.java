package com.cookiesstore.admin.service.customers;

public class CustomerAddressNotFoundException extends CustomerAddressDomainException {

    public CustomerAddressNotFoundException(Long addressId) {
        super("admin.customers.addresses.flash.notFound");
    }
}

