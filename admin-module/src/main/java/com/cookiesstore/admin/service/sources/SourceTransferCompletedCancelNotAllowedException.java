package com.cookiesstore.admin.service.sources;

public class SourceTransferCompletedCancelNotAllowedException extends SourceDomainException {

    public SourceTransferCompletedCancelNotAllowedException(Long transferId) {
        super("admin.product_sources.manage.transfers.error.completedCancelNotAllowed");
    }
}
