package com.cookiesstore.common.services.stock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.cookiesstore.common.entities.AdminSourceStockMovement;
import com.cookiesstore.common.entities.AdminSourceStockMovementType;
import com.cookiesstore.common.entities.ProductSource;
import com.cookiesstore.common.repositories.AdminSourceStockMovementRepository;
import com.cookiesstore.common.repositories.OrderRepository;
import com.cookiesstore.common.repositories.ProductRepository;
import com.cookiesstore.common.repositories.ProductSourceRepository;
import com.cookiesstore.common.repositories.SourceRepository;

@Service
public class StockMovementService
 {
    private final ProductSourceRepository productSourceRepository;
    private final AdminSourceStockMovementRepository adminSourceStockMovementRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final SourceRepository sourceRepository;

    public StockMovementService(
        ProductSourceRepository productSourceRepository,
        AdminSourceStockMovementRepository adminSourceStockMovementRepository,
        OrderRepository orderRepository,
        ProductRepository productRepository,
        SourceRepository sourceRepository
        )
    {
        this.productSourceRepository = productSourceRepository;
        this.adminSourceStockMovementRepository = adminSourceStockMovementRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.sourceRepository = sourceRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AdminSourceStockMovement registerSale(Long orderId, Long productId, Long sourceId, Integer quantityOrdered)
    {
        var currentStock = productSourceRepository.findByProductIdAndSourceId(productId, sourceId)
            .orElseThrow(() -> new StockMovementProductSourceNotFoundException(productId, sourceId));
        var movement = adminSourceStockMovementRepository.findByOrderIdAndProductIdAndSourceId(orderId, productId, sourceId);

        if (movement == null) {
            movement = new AdminSourceStockMovement();
            movement.setProduct(currentStock.getProduct());
            movement.setMovementType(AdminSourceStockMovementType.SALE);
            movement.setReferenceType(AdminSourceStockMovementType.SALE.toString());
            movement.setOrder(orderRepository.getReferenceById(orderId));
        }
        String referenceCode = currentStock.getSource().getCode() + "/" + orderId;
        movement.setSource(currentStock.getSource());
        movement.setQuantityDelta(-quantityOrdered);
        movement.setBalanceAfter(currentStock.getStockQuantity() - quantityOrdered);
        movement.setReferenceCode(referenceCode);
        return adminSourceStockMovementRepository.saveAndFlush(movement);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AdminSourceStockMovement registerAdjustmentModify(
        Long productId,
        Long sourceId,
        Integer balanceBefore,
        Integer balanceAfter,
        Long actorUserId
    )
    {
        var productSource = productSourceRepository.findByProductIdAndSourceId(productId, sourceId).orElse(null);
        if (productSource == null) {
            return null;
        }
        var movement = create(productId, productSource, balanceBefore, balanceAfter, actorUserId);
        return adminSourceStockMovementRepository.saveAndFlush(movement);
    }



    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AdminSourceStockMovement registerInitialStock(
        Long productId,
        Long sourceId,
        Integer balanceAfter,
        Long actorUserId
    )
    {
        var productSource = productSourceRepository.findByProductIdAndSourceId(productId, sourceId).orElse(null);
        if (productSource == null) {
            return null;
        }
        var movement = create(productId, productSource, 0, balanceAfter, actorUserId);
        return adminSourceStockMovementRepository.saveAndFlush(movement);
    }

     @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AdminSourceStockMovement registerAdjustmentDeletion(
        Long productId,
        Long sourceId,
        String sourceCode,
        Integer balanceBefore,
        Long actorUserId
    )
    {
        int previousBalance = balanceBefore == null ? 0 : balanceBefore;
        String referenceSourceCode = sourceCode == null ? String.valueOf(sourceId) : sourceCode;
        var movement = new AdminSourceStockMovement();
        movement.setProduct(productRepository.getReferenceById(productId));
        movement.setSource(sourceRepository.getReferenceById(sourceId));
        movement.setMovementType(AdminSourceStockMovementType.ADJUSTMENT);
        movement.setReferenceType(AdminSourceStockMovementType.ADJUSTMENT.toString());
        movement.setQuantityDelta(- previousBalance);
        movement.setBalanceAfter(0);
        movement.setReferenceCode(referenceSourceCode + "/" + productId);
        movement.setCreatedByAdminUserId(actorUserId);
        return adminSourceStockMovementRepository.saveAndFlush(movement);
    }

    private AdminSourceStockMovement create(
        Long productId,
        ProductSource productSource,
        Integer balanceBefore,
        Integer balanceAfter,
        Long actorUserId
    )
    {
         var movement = new AdminSourceStockMovement();
        movement.setProduct(productSource.getProduct());
        movement.setMovementType(AdminSourceStockMovementType.ADJUSTMENT);
        movement.setReferenceType(AdminSourceStockMovementType.ADJUSTMENT.toString());
        
        String referenceCode = productSource.getSource().getCode() + "/" + productId;
        movement.setSource(productSource.getSource());
        movement.setQuantityDelta(balanceAfter - balanceBefore);
        movement.setBalanceAfter(balanceAfter);
        movement.setReferenceCode(referenceCode);
        movement.setCreatedByAdminUserId(actorUserId);
        return movement;
    }
    
}
