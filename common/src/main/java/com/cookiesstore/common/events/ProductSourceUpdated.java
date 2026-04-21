package com.cookiesstore.common.events;

public record ProductSourceUpdated(
    Long productId,
    Long sourceId,
    Integer previousStockQuantity,
    Integer currentStockQuantity,
    Long actorUserId
) {
}
