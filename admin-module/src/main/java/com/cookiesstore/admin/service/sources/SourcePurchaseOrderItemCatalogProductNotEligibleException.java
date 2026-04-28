package com.cookiesstore.admin.service.sources;

public class SourcePurchaseOrderItemCatalogProductNotEligibleException extends SourceDomainException {

    public SourcePurchaseOrderItemCatalogProductNotEligibleException(Long productId) {
        super("admin.product_sources.manage.purchaseOrders.error.catalogProductNotEligible");
    }
}
