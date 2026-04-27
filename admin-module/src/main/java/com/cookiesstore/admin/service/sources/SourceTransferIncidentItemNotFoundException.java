package com.cookiesstore.admin.service.sources;

public class SourceTransferIncidentItemNotFoundException extends SourceDomainException {

    public SourceTransferIncidentItemNotFoundException(Long transferId, Long productId) {
        super("admin.product_sources.manage.transfers.error.incidentItemNotFound");
    }
}
