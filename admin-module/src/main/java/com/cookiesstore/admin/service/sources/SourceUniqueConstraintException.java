package com.cookiesstore.admin.service.sources;

public class SourceUniqueConstraintException extends SourceDomainException {

    public SourceUniqueConstraintException() {
        super("admin.sources.error.unique");
    }
}
