package com.cookiesstore.admin.service.sources;

public class SourceTransferNotFoundException extends SourceDomainException {

    public SourceTransferNotFoundException(Long transferId) {
        super("admin.product_sources.manage.transfers.error.transferNotFound");
    }
}
