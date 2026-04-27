package com.cookiesstore.admin.service.sources;

public class SourceTransferProductOverbookedException extends SourceDomainException {

    public SourceTransferProductOverbookedException(Long productId, Long sourceId) {
        super("admin.product_sources.manage.transfers.error.productOverbooked");
    }
}
