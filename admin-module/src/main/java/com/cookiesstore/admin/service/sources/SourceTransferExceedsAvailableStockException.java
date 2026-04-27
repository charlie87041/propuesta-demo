package com.cookiesstore.admin.service.sources;

public class SourceTransferExceedsAvailableStockException extends SourceDomainException {

    public SourceTransferExceedsAvailableStockException(Long productId, Long sourceId) {
        super("admin.product_sources.manage.transfers.error.exceedsAvailableStock");
    }
}
