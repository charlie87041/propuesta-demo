package com.cookiesstore.common.events;

public record ProductSourceRemoved(
    Long productId,
    Long sourceId,
    String sourceCode,
    Integer previousStockQuantity,
    Long actorUserId
) {
}
