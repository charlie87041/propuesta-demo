package com.cookiesstore.pos.listeners;

import java.time.LocalDate;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.cookiesstore.common.entities.AdminSourceStockMovementType;
import com.cookiesstore.common.entities.OrderStatus;
import com.cookiesstore.common.events.OrderUpdated;
import com.cookiesstore.common.repositories.AdminSourceStockMovementRepository;
import com.cookiesstore.pos.services.PosOrderHistoryService;
import com.cookiesstore.pos.services.PosSessionService;



public class OrderUpdatedListener {
    private final PosSessionService posSessionService;
    private final AdminSourceStockMovementRepository stockMovementRepository;
    private final PosOrderHistoryService posOrderHistoryService;

    public OrderUpdatedListener(
        PosSessionService posSessionService,
        AdminSourceStockMovementRepository stockMovementRepository,
        PosOrderHistoryService posOrderHistoryService
    ) {
        this.posSessionService = posSessionService;
        this.stockMovementRepository = stockMovementRepository;
        this.posOrderHistoryService = posOrderHistoryService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = false)
    public void onOrderUpdated(OrderUpdated event) {
        if (event.status() == OrderStatus.COMPLETED) {
            if (stockMovementRepository.existsByOrderIdAndMovementType(event.orderId(), AdminSourceStockMovementType.SALE)) {
                return;
            }
            var history =  posOrderHistoryService.findOrderPosSource(event.orderId());
           
        }
    }

 
}
