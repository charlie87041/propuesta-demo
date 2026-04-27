package com.cookiesstore.admin.service.sources;

public class SourceTransferDuplicateProductException extends SourceDomainException {

    public SourceTransferDuplicateProductException(Long productId) {
        super("admin.product_sources.manage.transfers.error.duplicateProduct");
    }
}
