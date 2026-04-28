package com.cookiesstore.admin.service.sources;

public class SourcePurchaseOrderSubmittableStatusRequiredException extends SourceDomainException {

    public SourcePurchaseOrderSubmittableStatusRequiredException(Long purchaseOrderId) {
        super("admin.product_sources.manage.purchaseOrders.error.submittableStatusRequired");
    }
}

