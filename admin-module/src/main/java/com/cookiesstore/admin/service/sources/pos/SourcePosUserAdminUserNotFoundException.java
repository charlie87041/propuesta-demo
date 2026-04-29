package com.cookiesstore.admin.service.sources.pos;

public class SourcePosUserAdminUserNotFoundException extends SourcePosUserDomainException {

    public SourcePosUserAdminUserNotFoundException(Long adminUserId) {
        super("Admin user not found: " + adminUserId);
    }
}
