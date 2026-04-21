package com.cookiesstore.common.listeners;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.cookiesstore.common.events.ProductSourceRemoved;
import com.cookiesstore.common.events.ProductSourceUpdated;
import com.cookiesstore.common.services.stock.StockMovementService;


@Component
public class ProductSourceListener {

    private final StockMovementService stockMovementService;

    public ProductSourceListener(StockMovementService stockMovementService)
    {
        this.stockMovementService = stockMovementService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = false)
    public void onProductSourceUpdated(ProductSourceUpdated event) {
       stockMovementService.registerAdjustmentModify(
                    event.productId(),
                    event.sourceId(),
                    event.previousStockQuantity(),
                    event.currentStockQuantity(),
                    event.actorUserId()
        );
    }

    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = false)
    public void onProductSourceRemoved(ProductSourceRemoved event) {
       stockMovementService.registerAdjustmentDeletion(
                    event.productId(),
                    event.sourceId(),
                    event.sourceCode(),
                    event.previousStockQuantity(),
                    event.actorUserId()
        );
    }
}
