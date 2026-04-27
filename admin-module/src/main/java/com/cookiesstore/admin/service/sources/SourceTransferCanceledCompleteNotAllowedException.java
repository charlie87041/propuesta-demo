package com.cookiesstore.admin.service.sources;

public class SourceTransferCanceledCompleteNotAllowedException extends SourceDomainException {

    public SourceTransferCanceledCompleteNotAllowedException(Long transferId) {
        super("admin.product_sources.manage.transfers.error.canceledCompleteNotAllowed");
    }
}
