package com.cookiesstore.common.listeners;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.cookiesstore.common.entities.AdminSourceStockMovementType;
import com.cookiesstore.common.entities.OrderStatus;
import com.cookiesstore.common.events.OrderUpdated;
import com.cookiesstore.common.repositories.AdminSourceStockMovementRepository;
import com.cookiesstore.common.repositories.ProductSourceRepository;
import com.cookiesstore.common.services.stock.StockMovementService;

import jakarta.annotation.Priority;


@Component
@Priority(10)
public class OrderUpdatedListener {
    private final StockMovementService stockMovementService;
    private final ProductSourceRepository productSourceRepository;
    private final AdminSourceStockMovementRepository stockMovementRepository;

    public OrderUpdatedListener(
        StockMovementService stockMovementService,
        ProductSourceRepository productSourceRepository,
        AdminSourceStockMovementRepository stockMovementRepository
    ) {
        this.stockMovementService = stockMovementService;
        this.productSourceRepository = productSourceRepository;
        this.stockMovementRepository = stockMovementRepository;

    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = false)
    public void onOrderUpdated(OrderUpdated event) {
        if (event.status() == OrderStatus.COMPLETED) {
            if (stockMovementRepository.existsByOrderIdAndMovementType(event.orderId(), AdminSourceStockMovementType.SALE)) {
                return;
            }
            event.items().stream()
                .filter(item -> item.productId() != null)
                .filter(item -> item.sourceId() != null)
                .filter(item -> item.quantityOrdered() != null && item.quantityOrdered() > 0)
                .forEach(item ->{
                    stockMovementService.registerSale(
                        event.orderId(),
                        item.productId(),
                        item.sourceId(),
                        item.quantityOrdered()
                    );
                    productSourceRepository.incrementTotalSold(
                        item.productId(),
                        item.sourceId(),
                        item.quantityOrdered().longValue()
                    );
            }
            );
        }
    }

 
}
