package com.cookiesstore.admin.service.sources;

public class SourcePurchaseOrderReceivableStatusRequiredException extends SourceDomainException {

    public SourcePurchaseOrderReceivableStatusRequiredException(Long purchaseOrderId) {
        super("admin.product_sources.manage.purchaseOrders.error.receivableStatusRequired");
    }
}

