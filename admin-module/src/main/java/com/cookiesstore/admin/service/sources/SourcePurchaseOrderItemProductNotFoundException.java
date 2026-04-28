package com.cookiesstore.admin.service.sources;

public class SourcePurchaseOrderItemProductNotFoundException extends SourceDomainException {

    public SourcePurchaseOrderItemProductNotFoundException(Long productId) {
        super("admin.product_sources.manage.purchaseOrders.error.catalogProductNotFound");
    }
}
