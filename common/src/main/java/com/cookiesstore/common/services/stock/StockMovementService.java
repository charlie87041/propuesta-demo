package com.cookiesstore.common.services.stock;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.cookiesstore.common.entities.AdminSourceStockMovement;
import com.cookiesstore.common.entities.AdminSourceStockMovementType;
import com.cookiesstore.common.entities.AdminSourceTransfer;
import com.cookiesstore.common.entities.ProductSource;
import com.cookiesstore.common.entities.ProductSourceStatus;
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

    @Transactional
    public AdminSourceStockMovement registerTransferIn(
        AdminSourceTransfer transfer,
        Long productId,
        Long destinationSourceId,
        Integer quantity,
        Long actorUserId,
        String note
    ) {
        if (quantity == null || quantity <= 0) {
            return null;
        }
        if (adminSourceStockMovementRepository.existsByTransferIdAndProductIdAndSourceIdAndMovementType(
            transfer.getId(),
            productId,
            destinationSourceId,
            AdminSourceStockMovementType.TRANSFER_IN
        )) {
            return null;
        }

        ProductSource destinationProductSource = productSourceRepository
            .findForUpdateByProductIdAndSourceId(productId, destinationSourceId)
            .orElseGet(() -> createDestinationProductSource(productId, destinationSourceId));

        int balanceBefore = destinationProductSource.getStockQuantity();
        int balanceAfter = balanceBefore + quantity;
        destinationProductSource.setStockQuantity(balanceAfter);
        productSourceRepository.save(destinationProductSource);

        AdminSourceStockMovement movement = new AdminSourceStockMovement();
        movement.setSource(destinationProductSource.getSource());
        movement.setProduct(destinationProductSource.getProduct());
        movement.setTransfer(transfer);
        movement.setMovementType(AdminSourceStockMovementType.TRANSFER_IN);
        movement.setReferenceType(AdminSourceStockMovementType.TRANSFER_IN.name());
        movement.setReferenceCode(transfer.getReferenceCode());
        movement.setQuantityDelta(quantity);
        movement.setBalanceAfter(balanceAfter);
        movement.setCreatedByAdminUserId(actorUserId);
        movement.setNote(note);
        movement.setMetadata("{\"transferId\":" + transfer.getId() + "}");

        return adminSourceStockMovementRepository.saveAndFlush(movement);
    }

    @Transactional
    public AdminSourceStockMovement registerTransferOut(
        AdminSourceTransfer transfer,
        Long productId,
        Long originSourceId,
        Integer quantity,
        Long actorUserId,
        String note
    ) {
        if (quantity == null || quantity <= 0) {
            return null;
        }
        if (adminSourceStockMovementRepository.existsByTransferIdAndProductIdAndSourceIdAndMovementType(
            transfer.getId(),
            productId,
            originSourceId,
            AdminSourceStockMovementType.TRANSFER_OUT
        )) {
            return null;
        }

        ProductSource originProductSource = productSourceRepository
            .findForUpdateByProductIdAndSourceId(productId, originSourceId)
            .orElseThrow(() -> new StockMovementProductSourceNotFoundException(productId, originSourceId));

        int balanceBefore = originProductSource.getStockQuantity();
        if (balanceBefore < quantity) {
            throw new StockMovementInsufficientStockException(productId, originSourceId, quantity, balanceBefore);
        }
        int balanceAfter = balanceBefore - quantity;
        originProductSource.setStockQuantity(balanceAfter);
        productSourceRepository.save(originProductSource);

        AdminSourceStockMovement movement = new AdminSourceStockMovement();
        movement.setSource(originProductSource.getSource());
        movement.setProduct(originProductSource.getProduct());
        movement.setTransfer(transfer);
        movement.setMovementType(AdminSourceStockMovementType.TRANSFER_OUT);
        movement.setReferenceType(AdminSourceStockMovementType.TRANSFER_OUT.name());
        movement.setReferenceCode(transfer.getReferenceCode());
        movement.setQuantityDelta(-quantity);
        movement.setBalanceAfter(balanceAfter);
        movement.setCreatedByAdminUserId(actorUserId);
        movement.setNote(note);
        movement.setMetadata("{\"transferId\":" + transfer.getId() + "}");

        return adminSourceStockMovementRepository.saveAndFlush(movement);
    }

    @Transactional
    public AdminSourceStockMovement registerTransferOutReversal(
        AdminSourceTransfer transfer,
        Long productId,
        Long originSourceId,
        Integer quantity,
        Long actorUserId,
        String note
    ) {
        if (quantity == null || quantity <= 0) {
            return null;
        }
        boolean hadTransferOut = adminSourceStockMovementRepository.existsByTransferIdAndProductIdAndSourceIdAndMovementType(
            transfer.getId(),
            productId,
            originSourceId,
            AdminSourceStockMovementType.TRANSFER_OUT
        );
        if (!hadTransferOut) {
            return null;
        }
        boolean alreadyReverted = adminSourceStockMovementRepository.existsByTransferIdAndProductIdAndSourceIdAndMovementType(
            transfer.getId(),
            productId,
            originSourceId,
            AdminSourceStockMovementType.TRANSFER_IN
        );
        if (alreadyReverted) {
            return null;
        }

        ProductSource originProductSource = productSourceRepository
            .findForUpdateByProductIdAndSourceId(productId, originSourceId)
            .orElseGet(() -> createDestinationProductSource(productId, originSourceId));

        int balanceBefore = originProductSource.getStockQuantity();
        int balanceAfter = balanceBefore + quantity;
        originProductSource.setStockQuantity(balanceAfter);
        productSourceRepository.save(originProductSource);

        AdminSourceStockMovement movement = new AdminSourceStockMovement();
        movement.setSource(originProductSource.getSource());
        movement.setProduct(originProductSource.getProduct());
        movement.setTransfer(transfer);
        movement.setMovementType(AdminSourceStockMovementType.TRANSFER_IN);
        movement.setReferenceType("TRANSFER_CANCEL_REVERT");
        movement.setReferenceCode(transfer.getReferenceCode());
        movement.setQuantityDelta(quantity);
        movement.setBalanceAfter(balanceAfter);
        movement.setCreatedByAdminUserId(actorUserId);
        movement.setNote(note);
        movement.setMetadata("{\"transferId\":" + transfer.getId() + ",\"reversal\":true}");

        return adminSourceStockMovementRepository.saveAndFlush(movement);
    }

    @Transactional
    public AdminSourceStockMovement registerPurchaseReceipt(
        Long purchaseOrderId,
        Long sourceId,
        Long productId,
        Integer quantity,
        Long unitCostMinor,
        Instant receivedAt,
        Long actorUserId,
        String note,
        String referenceCode
    ) {
        if (quantity == null || quantity <= 0) {
            return null;
        }

        ProductSource productSource = productSourceRepository
            .findForUpdateByProductIdAndSourceId(productId, sourceId)
            .orElseGet(() -> createDestinationProductSource(productId, sourceId));

        int balanceBefore = productSource.getStockQuantity();
        int balanceAfter = balanceBefore + quantity;
        productSource.setStockQuantity(balanceAfter);
        updatePurchaseCostSnapshots(productSource, balanceBefore, quantity, unitCostMinor, receivedAt);
        productSourceRepository.save(productSource);

        AdminSourceStockMovement movement = new AdminSourceStockMovement();
        movement.setSource(productSource.getSource());
        movement.setProduct(productSource.getProduct());
        movement.setMovementType(AdminSourceStockMovementType.PURCHASE_RECEIPT);
        movement.setReferenceType(AdminSourceStockMovementType.PURCHASE_RECEIPT.name());
        movement.setReferenceCode(referenceCode);
        movement.setQuantityDelta(quantity);
        movement.setBalanceAfter(balanceAfter);
        movement.setCreatedByAdminUserId(actorUserId);
        movement.setNote(note);
        movement.setMetadata("{\"purchaseOrderId\":" + purchaseOrderId + "}");

        return adminSourceStockMovementRepository.saveAndFlush(movement);
    }

    private ProductSource createDestinationProductSource(Long productId, Long destinationSourceId) {
        ProductSource productSource = new ProductSource();
        productSource.setProduct(productRepository.getReferenceById(productId));
        productSource.setSource(sourceRepository.getReferenceById(destinationSourceId));
        productSource.setStatus(ProductSourceStatus.ACTIVE);
        productSource.setStockQuantity(0);
        productSource.setLowStockThreshold(0);
        return productSourceRepository.saveAndFlush(productSource);
    }

    private void updatePurchaseCostSnapshots(
        ProductSource productSource,
        int balanceBefore,
        int receivedQuantity,
        Long unitCostMinor,
        Instant receivedAt
    ) {
        if (receivedQuantity <= 0 || unitCostMinor == null || unitCostMinor < 0) {
            return;
        }

        productSource.setLastPurchaseCostMinor(unitCostMinor);
        productSource.setLastPurchaseAt(receivedAt != null ? receivedAt : Instant.now());
        productSource.setAveragePurchaseCostMinor(
            calculateWeightedAverageMinor(
                balanceBefore,
                productSource.getAveragePurchaseCostMinor(),
                receivedQuantity,
                unitCostMinor
            )
        );
    }

    private long calculateWeightedAverageMinor(
        int balanceBefore,
        Long currentAverageMinor,
        int receivedQuantity,
        long receivedUnitCostMinor
    ) {
        if (receivedQuantity <= 0) {
            return currentAverageMinor == null ? 0L : currentAverageMinor;
        }
        if (balanceBefore <= 0 || currentAverageMinor == null) {
            return receivedUnitCostMinor;
        }

        BigDecimal previousValue = BigDecimal.valueOf(currentAverageMinor)
            .multiply(BigDecimal.valueOf(balanceBefore));
        BigDecimal receivedValue = BigDecimal.valueOf(receivedUnitCostMinor)
            .multiply(BigDecimal.valueOf(receivedQuantity));
        BigDecimal totalQuantity = BigDecimal.valueOf((long) balanceBefore + receivedQuantity);

        return previousValue
            .add(receivedValue)
            .divide(totalQuantity, 0, RoundingMode.HALF_UP)
            .longValue();
    }

    @Transactional
    public AdminSourceStockMovement registerIncidentRevertAdjustment(
        AdminSourceTransfer transfer,
        Long sourceId,
        Long productId,
        Integer quantityDelta,
        Long actorUserId,
        String note
    ) {
        if (quantityDelta == null || quantityDelta == 0) {
            return null;
        }

        ProductSource productSource = productSourceRepository
            .findForUpdateByProductIdAndSourceId(productId, sourceId)
            .orElseGet(() -> createDestinationProductSource(productId, sourceId));

        int balanceBefore = productSource.getStockQuantity();
        int balanceAfter = balanceBefore + quantityDelta;
        if (balanceAfter < 0) {
            throw new StockMovementInsufficientStockException(productId, sourceId, -quantityDelta, balanceBefore);
        }
        productSource.setStockQuantity(balanceAfter);
        productSourceRepository.save(productSource);

        AdminSourceStockMovement movement = new AdminSourceStockMovement();
        movement.setTransfer(transfer);
        movement.setSource(productSource.getSource());
        movement.setProduct(productSource.getProduct());
        movement.setMovementType(AdminSourceStockMovementType.ADJUSTMENT);
        movement.setReferenceType("TRANSFER_INCIDENT_REVERT");
        movement.setReferenceCode(transfer.getReferenceCode());
        movement.setQuantityDelta(quantityDelta);
        movement.setBalanceAfter(balanceAfter);
        movement.setCreatedByAdminUserId(actorUserId);
        movement.setNote(note);
        movement.setMetadata("{\"transferId\":" + transfer.getId() + ",\"incidentRevert\":true}");

        return adminSourceStockMovementRepository.saveAndFlush(movement);
    }

    public boolean hasTransferOutMovement(Long transferId, Long productId, Long sourceId) {
        return adminSourceStockMovementRepository.existsByTransferIdAndProductIdAndSourceIdAndMovementType(
            transferId,
            productId,
            sourceId,
            AdminSourceStockMovementType.TRANSFER_OUT
        );
    }
    
}
