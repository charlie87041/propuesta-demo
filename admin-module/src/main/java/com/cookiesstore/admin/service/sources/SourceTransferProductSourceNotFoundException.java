package com.cookiesstore.admin.service.sources;

public class SourceTransferProductSourceNotFoundException extends SourceDomainException {

    public SourceTransferProductSourceNotFoundException(Long productId, Long sourceId) {
        super("admin.product_sources.manage.transfers.error.productSourceNotFound");
    }
}
