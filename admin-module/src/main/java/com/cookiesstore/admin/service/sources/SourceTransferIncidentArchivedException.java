package com.cookiesstore.admin.service.sources;

public class SourceTransferIncidentArchivedException extends SourceDomainException {

    public SourceTransferIncidentArchivedException(Long incidentItemId) {
        super("admin.product_sources.manage.transfers.error.incidentArchived");
    }
}
