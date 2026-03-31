package com.cookiesstore.common.services.customers;

public class CustomerEmailExistsException extends CustomerDomainException {

    public CustomerEmailExistsException(String email) {
        super("admin.customers.error.email.exists");
    }
}
