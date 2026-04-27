package com.cookiesstore.admin.service.sources;

public class SourceTransferSameSourceException extends SourceDomainException {

    public SourceTransferSameSourceException(Long sourceId) {
        super("admin.product_sources.manage.transfers.error.sameSource");
    }
}
