package com.cookiesstore.admin.service.sources;

public class SourceTransferInsufficientStockException extends SourceDomainException {

    public SourceTransferInsufficientStockException(Long productId, Long sourceId) {
        super("admin.product_sources.manage.transfers.error.insufficientStock");
    }
}
