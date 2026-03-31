package com.cookiesstore.admin.service.users;

public class AdminUserEmailExistsException extends AdminUserDomainException {

    public AdminUserEmailExistsException(String email) {
        super("admin.users.error.email.exists");
    }
}
