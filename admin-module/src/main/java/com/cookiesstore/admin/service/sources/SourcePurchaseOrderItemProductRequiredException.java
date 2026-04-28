package com.cookiesstore.admin.service.sources;

public class SourcePurchaseOrderItemProductRequiredException extends SourceDomainException {

    public SourcePurchaseOrderItemProductRequiredException() {
        super("admin.product_sources.manage.purchaseOrders.error.catalogProductRequired");
    }
}
