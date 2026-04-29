package com.cookiesstore.admin.service.sources.pos;

public abstract class SourcePosConfigDomainException extends IllegalArgumentException {

    protected SourcePosConfigDomainException(String message) {
        super(message);
    }
}
