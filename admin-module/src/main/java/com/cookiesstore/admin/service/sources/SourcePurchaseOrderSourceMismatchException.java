package com.cookiesstore.admin.service.sources;

public class SourcePurchaseOrderSourceMismatchException extends SourceDomainException {

    public SourcePurchaseOrderSourceMismatchException(Long purchaseOrderId, Long sourceId) {
        super("admin.product_sources.manage.purchaseOrders.error.sourceMismatch");
    }
}
