package com.cookiesstore.admin.service.sources;

public class SourceTransferIncidentNotFoundException extends SourceDomainException {

    public SourceTransferIncidentNotFoundException(Long incidentItemId) {
        super("admin.product_sources.manage.transfers.error.incidentNotFound");
    }
}
