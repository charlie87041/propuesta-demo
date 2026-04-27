package com.cookiesstore.admin.service.sources;

public class SourceTransferDraftRequiredException extends SourceDomainException {

    public SourceTransferDraftRequiredException(Long transferId) {
        super("admin.product_sources.manage.transfers.error.draftRequired");
    }
}
