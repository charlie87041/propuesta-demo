package com.cookiesstore.admin.service.sources.pos;

public class SourcePosUserAlreadyAssignedException extends SourcePosUserDomainException {

    public SourcePosUserAlreadyAssignedException(Long sourceId, Long adminUserId) {
        super("Admin user " + adminUserId + " is already assigned to source " + sourceId);
    }
}
