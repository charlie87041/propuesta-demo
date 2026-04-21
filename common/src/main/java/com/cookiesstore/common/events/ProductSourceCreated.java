package com.cookiesstore.common.events;

public record ProductSourceCreated(
    Long productId,
    Long sourceId,
    Integer currentStockQuantity,
    Long actorUserId
) {
}
