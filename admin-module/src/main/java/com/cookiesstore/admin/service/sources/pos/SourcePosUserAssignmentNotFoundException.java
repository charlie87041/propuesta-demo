package com.cookiesstore.admin.service.sources.pos;

public class SourcePosUserAssignmentNotFoundException extends SourcePosUserDomainException {

    public SourcePosUserAssignmentNotFoundException(Long relationId) {
        super("POS assignment not found: " + relationId);
    }
}
