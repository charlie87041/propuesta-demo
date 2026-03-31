package com.cookiesstore.admin.service.sources;

public class SourceCodeExistsException extends SourceDomainException {

    public SourceCodeExistsException(String code) {
        super("admin.sources.error.code.exists");
    }
}
