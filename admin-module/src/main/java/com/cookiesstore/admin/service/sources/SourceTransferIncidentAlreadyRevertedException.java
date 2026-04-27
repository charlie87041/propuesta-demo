package com.cookiesstore.admin.service.sources;

public class SourceTransferIncidentAlreadyRevertedException extends SourceDomainException {

    public SourceTransferIncidentAlreadyRevertedException(Long incidentItemId) {
        super("admin.product_sources.manage.transfers.error.incidentAlreadyReverted");
    }
}
