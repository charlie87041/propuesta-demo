package com.cookiesstore.admin.service.sources;

public class SourcePurchaseOrderItemNameRequiredException extends SourceDomainException {

    public SourcePurchaseOrderItemNameRequiredException() {
        super("admin.product_sources.manage.purchaseOrders.error.adHocNameRequired");
    }
}
