package com.cookiesstore.admin.service.users;

public class AdminUserDomainMissingException extends AdminUserDomainException {

    public AdminUserDomainMissingException() {
        super("admin.users.error.domain.missing");
    }
}
