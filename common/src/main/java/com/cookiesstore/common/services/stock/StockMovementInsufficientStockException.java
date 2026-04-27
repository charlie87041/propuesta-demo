package com.cookiesstore.common.services.stock;

public class StockMovementInsufficientStockException extends StockMovementDomainException {

    public StockMovementInsufficientStockException(
        Long productId,
        Long sourceId,
        Integer requestedQuantity,
        Integer availableQuantity
    ) {
        super("Cannot register transfer out movement: insufficient stock"
            + " (productId=%d, sourceId=%d, requested=%d, available=%d)"
                .formatted(productId, sourceId, requestedQuantity, availableQuantity));
    }
}
