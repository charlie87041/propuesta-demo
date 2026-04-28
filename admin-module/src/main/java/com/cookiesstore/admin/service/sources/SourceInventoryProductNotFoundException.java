package com.cookiesstore.admin.service.sources;

public class SourceInventoryProductNotFoundException extends SourceDomainException {

    public SourceInventoryProductNotFoundException(Long sourceId, Long productId) {
        super("admin.product_sources.manage.inventory.error.productNotFoundInSource");
    }
}
