package com.cookiesstore.common.listeners;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.cookiesstore.common.entities.OrderStatus;
import com.cookiesstore.common.events.OrderUpdated;
import com.cookiesstore.common.services.stock.StockMovementService;


@Component
public class OrderUpdatedListener {
    private final StockMovementService stockMovementService;

    public OrderUpdatedListener(StockMovementService stockMovementService)
    {
        this.stockMovementService = stockMovementService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = false)
    public void onOrderUpdated(OrderUpdated event) {
        if (event.status() == OrderStatus.COMPLETED) {
            event.items().stream()
                .filter(item -> item.productId() != null)
                .filter(item -> item.sourceId() != null)
                .filter(item -> item.quantityOrdered() != null && item.quantityOrdered() > 0)
                .forEach(item ->
                stockMovementService.registerSale(
                    event.orderId(),
                    item.productId(),
                    item.sourceId(),
                    item.quantityOrdered()
                )
            );
        }
    }

 
}
