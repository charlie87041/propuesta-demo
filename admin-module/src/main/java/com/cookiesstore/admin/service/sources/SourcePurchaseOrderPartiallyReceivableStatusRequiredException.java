package com.cookiesstore.admin.service.sources;

public class SourcePurchaseOrderPartiallyReceivableStatusRequiredException extends SourceDomainException {

    public SourcePurchaseOrderPartiallyReceivableStatusRequiredException(Long purchaseOrderId) {
        super("admin.product_sources.manage.purchaseOrders.error.partiallyReceivableStatusRequired");
    }
}

