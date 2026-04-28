package com.cookiesstore.admin.service.sources;

public class SourcePurchaseOrderReceiptProductRequiredException extends SourceDomainException {

    public SourcePurchaseOrderReceiptProductRequiredException(Long purchaseOrderId) {
        super("admin.product_sources.manage.purchaseOrders.error.receiptProductRequired");
    }
}

