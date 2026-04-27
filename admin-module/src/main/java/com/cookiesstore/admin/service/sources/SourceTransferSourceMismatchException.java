package com.cookiesstore.admin.service.sources;

public class SourceTransferSourceMismatchException extends SourceDomainException {

    public SourceTransferSourceMismatchException(Long transferId, Long sourceId) {
        super("admin.product_sources.manage.transfers.error.sourceMismatch");
    }
}
