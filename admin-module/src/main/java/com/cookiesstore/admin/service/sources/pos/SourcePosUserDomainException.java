package com.cookiesstore.admin.service.sources.pos;

public abstract class SourcePosUserDomainException extends IllegalArgumentException {

    protected SourcePosUserDomainException(String message) {
        super(message);
    }
}
