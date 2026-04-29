package com.cookiesstore.admin.service.sources.pos;

public class SourcePosUserSourceNotFoundException extends SourcePosUserDomainException {

    public SourcePosUserSourceNotFoundException(Long sourceId) {
        super("Source not found: " + sourceId);
    }
}
