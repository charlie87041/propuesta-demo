package com.cookiesstore.admin.service.sources.pos;

public class SourcePosUserSourceMismatchException extends SourcePosUserDomainException {

    public SourcePosUserSourceMismatchException(Long sourceId, Long relationId) {
        super("POS assignment " + relationId + " does not belong to source " + sourceId);
    }
}
