package com.cookiesstore.common.services.stock;

public class StockMovementProductSourceNotFoundException extends StockMovementDomainException {

    public StockMovementProductSourceNotFoundException(Long productId, Long sourceId) {
        super("Cannot register stock movement: product source relation not found (productId=%d, sourceId=%d)"
            .formatted(productId, sourceId));
    }
}
