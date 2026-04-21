package com.cookiesstore.common.events;

import com.cookiesstore.common.entities.OrderStatus;
import java.util.List;
import java.util.Objects;

public record OrderUpdated(
    Long orderId,
    OrderStatus status,
    List<OrderItemSnapshot> items
) {

    public OrderUpdated {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        items = items == null ? List.of() : List.copyOf(items);
    }

    public record OrderItemSnapshot(
        Long productId,
        Long sourceId,
        Integer quantityOrdered
    ) {
    }
}
