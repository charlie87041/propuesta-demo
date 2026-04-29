package com.cookiesstore.admin.service.sources.pos;

public class SourcePosUserAdminUserInactiveException extends SourcePosUserDomainException {

    public SourcePosUserAdminUserInactiveException(Long adminUserId) {
        super("Admin user is inactive: " + adminUserId);
    }
}
