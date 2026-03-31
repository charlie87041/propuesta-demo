package com.cookiesstore.admin.service.users;

public class AdminUserNotFoundException extends AdminUserDomainException {

    public AdminUserNotFoundException(Long userId) {
        super("admin.users.error.notFound");
    }
}
