package com.cookiesstore.admin.service.sources;

public class SourceTransferIncidentMissingQuantityInvalidException extends SourceDomainException {

    public SourceTransferIncidentMissingQuantityInvalidException(Long transferId, Long productId) {
        super("admin.product_sources.manage.transfers.error.incidentMissingQuantityInvalid");
    }
}
