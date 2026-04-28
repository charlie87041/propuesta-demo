package com.cookiesstore.admin.service.sources;

public class SourcePurchaseOrderDraftRequiredException extends SourceDomainException {

    public SourcePurchaseOrderDraftRequiredException(Long purchaseOrderId) {
        super("admin.product_sources.manage.purchaseOrders.error.draftRequired");
    }
}
