package com.cookiesstore.admin.service.sources;

public class SourcePurchaseOrderAdhocResolutionStatusRequiredException extends SourceDomainException {

    public SourcePurchaseOrderAdhocResolutionStatusRequiredException(Long purchaseOrderId) {
        super("admin.product_sources.manage.purchaseOrders.error.adhocResolutionStatusRequired");
    }
}

