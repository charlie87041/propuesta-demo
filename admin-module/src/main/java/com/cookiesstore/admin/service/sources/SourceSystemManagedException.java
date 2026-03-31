package com.cookiesstore.admin.service.sources;

public class SourceSystemManagedException extends SourceDomainException {

    public SourceSystemManagedException(Long sourceId) {
        super("admin.sources.error.systemManaged");
    }
}
