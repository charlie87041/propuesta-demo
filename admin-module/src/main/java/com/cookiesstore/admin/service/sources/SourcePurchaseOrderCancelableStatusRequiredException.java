package com.cookiesstore.admin.service.sources;

public class SourcePurchaseOrderCancelableStatusRequiredException extends SourceDomainException {

    public SourcePurchaseOrderCancelableStatusRequiredException(Long purchaseOrderId) {
        super("admin.product_sources.manage.purchaseOrders.error.cancelableStatusRequired");
    }
}

