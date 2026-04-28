package com.cookiesstore.admin.service.sources;

public class SourcePurchaseOrderNotFoundException extends SourceDomainException {

    public SourcePurchaseOrderNotFoundException(Long purchaseOrderId) {
        super("admin.product_sources.manage.purchaseOrders.error.notFound");
    }
}
