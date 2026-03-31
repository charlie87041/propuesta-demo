package com.cookiesstore.common.services.customers;

public class CustomerNotFoundException extends CustomerDomainException {

    public CustomerNotFoundException(Long customerId) {
        super("admin.customers.flash.notFound");
    }
}
