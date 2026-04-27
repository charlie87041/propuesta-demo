package com.cookiesstore.admin.service.sources;

import java.time.Instant;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.cookiesstore.admin.web.dto.sources.CreateSourceTransfer;
import com.cookiesstore.admin.web.dto.sources.SourceTransferItem;
import com.cookiesstore.admin.web.dto.sources.TransferIncidentActionForm;
import com.cookiesstore.admin.web.dto.sources.TransferIncidentItemForm;
import com.cookiesstore.common.entities.AdminSourceTransferIncident;
import com.cookiesstore.common.entities.AdminSourceTransferIncidentItem;
import com.cookiesstore.common.entities.AdminSourceTransferIncidentType;
import com.cookiesstore.common.entities.AdminSourceTransfer;
import com.cookiesstore.common.entities.AdminSourceTransferItem;
import com.cookiesstore.common.entities.AdminSourceTransferStatus;
import com.cookiesstore.common.entities.AdminSourceTransferStatusHistory;
import com.cookiesstore.common.entities.ProductSource;
import com.cookiesstore.common.entities.Source;
import com.cookiesstore.common.repositories.AdminSourceTransferIncidentItemRepository;
import com.cookiesstore.common.repositories.AdminSourceTransferIncidentRepository;
import com.cookiesstore.common.repositories.AdminSourceTransferItemRepository;
import com.cookiesstore.common.repositories.AdminSourceTransferRepository;
import com.cookiesstore.common.repositories.AdminSourceTransferStatusHistoryRepository;
import com.cookiesstore.common.repositories.ProductSourceRepository;
import com.cookiesstore.common.repositories.SourceRepository;
import com.cookiesstore.common.services.stock.StockMovementInsufficientStockException;
import com.cookiesstore.common.services.stock.StockMovementProductSourceNotFoundException;
import com.cookiesstore.common.services.stock.StockMovementService;

import jakarta.transaction.Transactional;

@Service
public class SourceTransferService {

    private static final Set<AdminSourceTransferStatus> ON_GOING_STATUSES = EnumSet.of(
        AdminSourceTransferStatus.DRAFT,
        AdminSourceTransferStatus.IN_TRANSIT
    );

    private final AdminSourceTransferRepository adminSourceTransferRepository;
    private final AdminSourceTransferStatusHistoryRepository transferStatusHistoryRepository;
    private final ProductSourceRepository productSourceRepository;
    private final SourceRepository sourceRepository;
    private final AdminSourceTransferItemRepository transferItemRepository;
    private final AdminSourceTransferIncidentRepository transferIncidentRepository;
    private final AdminSourceTransferIncidentItemRepository transferIncidentItemRepository;
    private final StockMovementService stockMovementService;

    public SourceTransferService(
        AdminSourceTransferRepository adminSourceTransferRepository,
        AdminSourceTransferStatusHistoryRepository transferStatusHistoryRepository,
        ProductSourceRepository productSourceRepository,
        SourceRepository sourceRepository,
        AdminSourceTransferItemRepository transferItemRepository,
        AdminSourceTransferIncidentRepository transferIncidentRepository,
        AdminSourceTransferIncidentItemRepository transferIncidentItemRepository,
        StockMovementService stockMovementService
    ) {
        this.adminSourceTransferRepository = adminSourceTransferRepository;
        this.transferStatusHistoryRepository = transferStatusHistoryRepository;
        this.productSourceRepository = productSourceRepository;
        this.sourceRepository = sourceRepository;
        this.transferItemRepository = transferItemRepository;
        this.transferIncidentRepository = transferIncidentRepository;
        this.transferIncidentItemRepository = transferIncidentItemRepository;
        this.stockMovementService = stockMovementService;
    }

    @Transactional
    public AdminSourceTransfer startTransfer(CreateSourceTransfer form, Long sourceId, Long currentUserId) {
        Source origin = sourceRepository.findById(sourceId)
            .orElseThrow(() -> new SourceNotFoundException(sourceId));
        Source destination = sourceRepository.findById(form.getDestinationSourceId())
            .orElseThrow(() -> new SourceNotFoundException(form.getDestinationSourceId()));

        if (origin.getId().equals(destination.getId())) {
            throw new SourceTransferSameSourceException(sourceId);
        }

        List<SourceTransferItem> sortedItems = form.getItems().stream()
            .sorted(Comparator.comparing(SourceTransferItem::getProductId))
            .toList();

        Set<Long> requestedProductIds = new HashSet<>();
        Map<SourceTransferItem, ProductSource> productByItem = new HashMap<>();
        sortedItems.forEach(item -> {
            if (!requestedProductIds.add(item.getProductId())) {
                throw new SourceTransferDuplicateProductException(item.getProductId());
            }
            var product = this.productSourceRepository.findForUpdateByProductIdAndSourceId(item.getProductId(), sourceId)
                .orElseThrow(() -> new SourceTransferProductSourceNotFoundException(item.getProductId(), sourceId));

            productByItem.put(item, product);
            if (product.getStockQuantity() < item.getAmount()) {
                throw new SourceTransferInsufficientStockException(item.getProductId(), sourceId);
            }
            int totalStockInTransfer = transferItemRepository
                .sumQuantityByProductAndSourceAndTransferStatuses(item.getProductId(), sourceId, ON_GOING_STATUSES);
            if (totalStockInTransfer >= product.getStockQuantity()) {
                throw new SourceTransferProductOverbookedException(item.getProductId(), sourceId);
            }
            if (totalStockInTransfer + item.getAmount() > product.getStockQuantity()) {
                throw new SourceTransferExceedsAvailableStockException(item.getProductId(), sourceId);
            }
        });

        AdminSourceTransfer transfer = new AdminSourceTransfer();
        transfer.setNotes(form.getNotes());
        transfer.setReferenceCode(generateReferenceCode());
        transfer.setSourceFrom(origin);
        transfer.setSourceTo(destination);
        transfer.setTotalItems(sortedItems.size());
        transfer.setStatus(AdminSourceTransferStatus.DRAFT);
        transfer.setTotalQuantity(sortedItems.stream().mapToInt(SourceTransferItem::getAmount).sum());
        Instant createdAt = Instant.now();

        adminSourceTransferRepository.save(transfer);
        recordStatusTransition(transfer, null, AdminSourceTransferStatus.DRAFT, currentUserId, createdAt, null);

        List<AdminSourceTransferItem> transferItems = sortedItems.stream()
            .map(item -> transferItemFromForm(item, transfer, productByItem.get(item)))
            .toList();
        transferItemRepository.saveAll(transferItems);

        return adminSourceTransferRepository.findById(transfer.getId())
            .orElseThrow(() -> new SourceTransferNotFoundException(transfer.getId()));

    }

    @Transactional
    public void deleteDraftTransfer(Long sourceId, Long transferId) {
        AdminSourceTransfer transfer = adminSourceTransferRepository.findForUpdateById(transferId)
            .orElseThrow(() -> new SourceTransferNotFoundException(transferId));
        validateTransferBelongsToSource(transfer, sourceId);

        if (transfer.getStatus() != AdminSourceTransferStatus.DRAFT) {
            throw new SourceTransferDraftRequiredException(transferId);
        }

        adminSourceTransferRepository.delete(transfer);
    }

    @Transactional
    public AdminSourceTransfer markTransferInTransit(Long sourceId, Long transferId, Long currentUserId) {
        AdminSourceTransfer transfer = adminSourceTransferRepository.findForUpdateById(transferId)
            .orElseThrow(() -> new SourceTransferNotFoundException(transferId));
        validateTransferBelongsToSource(transfer, sourceId);

        if (transfer.getStatus() != AdminSourceTransferStatus.DRAFT) {
            throw new SourceTransferDraftRequiredException(transferId);
        }

        List<AdminSourceTransferItem> transferItems = transferItemRepository.findByTransferId(transfer.getId());
        applyOriginStockDeduction(transfer, transferItems, currentUserId, Map.of(), true);

        AdminSourceTransferStatus previousStatus = transfer.getStatus();
        transfer.setStatus(AdminSourceTransferStatus.IN_TRANSIT);
        Instant changedAt = Instant.now();
        AdminSourceTransfer updated = adminSourceTransferRepository.save(transfer);
        recordStatusTransition(updated, previousStatus, AdminSourceTransferStatus.IN_TRANSIT, currentUserId, changedAt, null);
        return updated;
    }

    @Transactional
    public AdminSourceTransfer cancelTransfer(Long sourceId, Long transferId, Long currentUserId) {
        return cancelTransferInternal(sourceId, transferId, currentUserId, null, null);
    }

    @Transactional
    public AdminSourceTransfer cancelTransferWithIncident(
        Long sourceId,
        Long transferId,
        Long currentUserId,
        TransferIncidentActionForm incidentForm
    ) {
        return cancelTransferInternal(
            sourceId,
            transferId,
            currentUserId,
            incidentForm,
            AdminSourceTransferIncidentType.CANCEL
        );
    }

    private AdminSourceTransfer cancelTransferInternal(
        Long sourceId,
        Long transferId,
        Long currentUserId,
        TransferIncidentActionForm incidentForm,
        AdminSourceTransferIncidentType incidentType
    ) {
        AdminSourceTransfer transfer = adminSourceTransferRepository.findForUpdateById(transferId)
            .orElseThrow(() -> new SourceTransferNotFoundException(transferId));
        validateTransferInvolvedSource(transfer, sourceId);

        if (transfer.getStatus() == AdminSourceTransferStatus.COMPLETED) {
            throw new SourceTransferCompletedCancelNotAllowedException(transferId);
        }

        AdminSourceTransferStatus previousStatus = transfer.getStatus();
        if (previousStatus == AdminSourceTransferStatus.CANCELED) {
            return transfer;
        }

        List<AdminSourceTransferItem> transferItems = transferItemRepository.findByTransferId(transfer.getId());
        Map<Long, Integer> missingByProduct = buildMissingByProduct(transferId, transferItems, incidentForm);
        boolean hasIncident = incidentType != null;
        if (previousStatus == AdminSourceTransferStatus.IN_TRANSIT) {
            revertOriginStockDeduction(
                transfer,
                transferItems,
                currentUserId,
                hasIncident ? toReturnedByProduct(transferItems, missingByProduct) : expectedByProduct(transferItems)
            );
        }

        transfer.setStatus(AdminSourceTransferStatus.CANCELED);
        Instant changedAt = Instant.now();
        AdminSourceTransfer updated = adminSourceTransferRepository.save(transfer);
        String incidentDescription = hasIncident ? incidentForm.getDescription() : null;
        recordStatusTransition(
            updated,
            previousStatus,
            AdminSourceTransferStatus.CANCELED,
            currentUserId,
            changedAt,
            incidentDescription
        );
        if (hasIncident) {
            recordIncident(
                updated,
                transferItems,
                missingByProduct,
                incidentType,
                incidentForm.getDescription(),
                currentUserId
            );
        }
        return updated;
    }

    @Transactional
    public AdminSourceTransfer completeTransfer(Long sourceId, Long transferId, Long currentUserId) {
        return completeTransferInternal(sourceId, transferId, currentUserId, null, null);
    }

    @Transactional
    public void closeIncident(Long sourceId, Long incidentItemId, Long currentUserId) {
        AdminSourceTransferIncidentItem incidentItem = transferIncidentItemRepository.findForUpdateById(incidentItemId)
            .orElseThrow(() -> new SourceTransferIncidentNotFoundException(incidentItemId));
        validateIncidentBelongsToSource(incidentItem, sourceId);
        if (incidentItem.isArchived()) {
            throw new SourceTransferIncidentArchivedException(incidentItemId);
        }
        incidentItem.setArchived(true);
        incidentItem.setClosedAt(Instant.now());
        incidentItem.setClosedByAdminUserId(currentUserId);
        transferIncidentItemRepository.save(incidentItem);
    }

    @Transactional
    public void revertIncident(Long sourceId, Long incidentItemId, Long currentUserId) {
        AdminSourceTransferIncidentItem incidentItem = transferIncidentItemRepository.findForUpdateById(incidentItemId)
            .orElseThrow(() -> new SourceTransferIncidentNotFoundException(incidentItemId));
        validateIncidentBelongsToSource(incidentItem, sourceId);
        if (incidentItem.isArchived()) {
            throw new SourceTransferIncidentArchivedException(incidentItemId);
        }
        if (incidentItem.getRevertedAt() != null) {
            throw new SourceTransferIncidentAlreadyRevertedException(incidentItemId);
        }
        if (incidentItem.getProduct() == null) {
            throw new SourceTransferIncidentItemNotFoundException(
                incidentItem.getIncident().getTransfer().getId(),
                null
            );
        }

        AdminSourceTransfer transfer = incidentItem.getIncident().getTransfer();
        Long productId = incidentItem.getProduct().getId();
        int missing = incidentItem.getMissingQuantity() == null ? 0 : incidentItem.getMissingQuantity();
        if (missing > 0) {
            if (incidentItem.getIncident().getIncidentType() == AdminSourceTransferIncidentType.COMPLETE) {
                stockMovementService.registerIncidentRevertAdjustment(
                    transfer,
                    transfer.getSourceFrom().getId(),
                    productId,
                    -missing,
                    currentUserId,
                    "Transfer incident revert (origin adjustment)"
                );
                stockMovementService.registerIncidentRevertAdjustment(
                    transfer,
                    transfer.getSourceTo().getId(),
                    productId,
                    missing,
                    currentUserId,
                    "Transfer incident revert (destination adjustment)"
                );
            } else if (
                incidentItem.getIncident().getIncidentType() == AdminSourceTransferIncidentType.CANCEL
                    && stockMovementService.hasTransferOutMovement(
                        transfer.getId(),
                        productId,
                        transfer.getSourceFrom().getId()
                    )
            ) {
                stockMovementService.registerIncidentRevertAdjustment(
                    transfer,
                    transfer.getSourceFrom().getId(),
                    productId,
                    missing,
                    currentUserId,
                    "Transfer cancel incident revert (origin adjustment)"
                );
            }
        }

        incidentItem.setRevertedAt(Instant.now());
        incidentItem.setRevertedByAdminUserId(currentUserId);
        transferIncidentItemRepository.save(incidentItem);
    }

    @Transactional
    public AdminSourceTransfer completeTransferWithIncident(
        Long sourceId,
        Long transferId,
        Long currentUserId,
        TransferIncidentActionForm incidentForm
    ) {
        return completeTransferInternal(
            sourceId,
            transferId,
            currentUserId,
            incidentForm,
            AdminSourceTransferIncidentType.COMPLETE
        );
    }

    private AdminSourceTransfer completeTransferInternal(
        Long sourceId,
        Long transferId,
        Long currentUserId,
        TransferIncidentActionForm incidentForm,
        AdminSourceTransferIncidentType incidentType
    ) {
        AdminSourceTransfer transfer = adminSourceTransferRepository.findForUpdateById(transferId)
            .orElseThrow(() -> new SourceTransferNotFoundException(transferId));
        validateTransferInvolvedSource(transfer, sourceId);

        if (transfer.getStatus() == AdminSourceTransferStatus.CANCELED) {
            throw new SourceTransferCanceledCompleteNotAllowedException(transferId);
        }

        AdminSourceTransferStatus previousStatus = transfer.getStatus();
        if (previousStatus != AdminSourceTransferStatus.COMPLETED) {
            List<AdminSourceTransferItem> transferItems = transferItemRepository.findByTransferId(transferId);
            Map<Long, Integer> missingByProduct = buildMissingByProduct(transferId, transferItems, incidentForm);
            Map<Long, Integer> receivedByProduct = toReceivedByProduct(transferItems, missingByProduct);

            if (previousStatus == AdminSourceTransferStatus.IN_TRANSIT) {
                revertOriginStockDeduction(transfer, transferItems, currentUserId, missingByProduct);
            } else {
                applyOriginStockDeduction(transfer, transferItems, currentUserId, missingByProduct, true);
            }

            for (AdminSourceTransferItem transferItem : transferItems) {
                if (transferItem.getProduct() == null) {
                    continue;
                }
                int receivedQty = receivedByProduct.getOrDefault(transferItem.getProduct().getId(), 0);
                if (receivedQty <= 0) {
                    continue;
                }
                stockMovementService.registerTransferIn(
                    transfer,
                    transferItem.getProduct().getId(),
                    transfer.getSourceTo().getId(),
                    receivedQty,
                    currentUserId,
                    transferItem.getNote()
                );
            }
            Instant changedAt = Instant.now();
            transfer.setStatus(AdminSourceTransferStatus.COMPLETED);
            AdminSourceTransfer updated = adminSourceTransferRepository.save(transfer);
            String incidentDescription = incidentType == null ? null : incidentForm.getDescription();
            recordStatusTransition(
                updated,
                previousStatus,
                AdminSourceTransferStatus.COMPLETED,
                currentUserId,
                changedAt,
                incidentDescription
            );
            if (incidentType != null) {
                recordIncident(
                    updated,
                    transferItems,
                    missingByProduct,
                    incidentType,
                    incidentForm.getDescription(),
                    currentUserId
                );
            }
            return updated;
        }

        return transfer;
    }

    private void applyOriginStockDeduction(
        AdminSourceTransfer transfer,
        List<AdminSourceTransferItem> transferItems,
        Long currentUserId,
        Map<Long, Integer> missingByProduct,
        boolean deductOnlyReceived
    ) {
        for (AdminSourceTransferItem transferItem : transferItems) {
            if (transferItem.getProduct() == null) {
                continue;
            }
            Long productId = transferItem.getProduct().getId();
            int expectedQty = transferItem.getQuantity();
            int missingQty = missingByProduct.getOrDefault(productId, 0);
            int discountQty = deductOnlyReceived ? Math.max(0, expectedQty - missingQty) : expectedQty;
            if (discountQty <= 0) {
                continue;
            }
            try {
                stockMovementService.registerTransferOut(
                    transfer,
                    productId,
                    transfer.getSourceFrom().getId(),
                    discountQty,
                    currentUserId,
                    transferItem.getNote()
                );
            } catch (StockMovementProductSourceNotFoundException ex) {
                throw new SourceTransferProductSourceNotFoundException(
                    productId,
                    transfer.getSourceFrom().getId()
                );
            } catch (StockMovementInsufficientStockException ex) {
                throw new SourceTransferInsufficientStockException(
                    productId,
                    transfer.getSourceFrom().getId()
                );
            }
        }
    }

    private void revertOriginStockDeduction(
        AdminSourceTransfer transfer,
        List<AdminSourceTransferItem> transferItems,
        Long currentUserId,
        Map<Long, Integer> quantitiesToRevertByProduct
    ) {
        for (AdminSourceTransferItem transferItem : transferItems) {
            if (transferItem.getProduct() == null) {
                continue;
            }
            Long productId = transferItem.getProduct().getId();
            int revertQty = quantitiesToRevertByProduct.getOrDefault(productId, 0);
            if (revertQty <= 0) {
                continue;
            }
            stockMovementService.registerTransferOutReversal(
                transfer,
                productId,
                transfer.getSourceFrom().getId(),
                revertQty,
                currentUserId,
                transferItem.getNote()
            );
        }
    }

    private Map<Long, Integer> buildMissingByProduct(
        Long transferId,
        List<AdminSourceTransferItem> transferItems,
        TransferIncidentActionForm incidentForm
    ) {
        Map<Long, Integer> missingByProduct = new HashMap<>();
        if (incidentForm == null || incidentForm.getItems() == null) {
            return missingByProduct;
        }

        Map<Long, Integer> expectedByProduct = expectedByProduct(transferItems);
        Set<Long> seen = new HashSet<>();
        for (TransferIncidentItemForm item : incidentForm.getItems()) {
            Long productId = item.getProductId();
            if (productId == null || !seen.add(productId)) {
                continue;
            }
            Integer expected = expectedByProduct.get(productId);
            if (expected == null) {
                throw new SourceTransferIncidentItemNotFoundException(transferId, productId);
            }
            int missing = item.getMissingQuantity() == null ? 0 : item.getMissingQuantity();
            if (missing < 0 || missing > expected) {
                throw new SourceTransferIncidentMissingQuantityInvalidException(transferId, productId);
            }
            missingByProduct.put(productId, missing);
        }
        return missingByProduct;
    }

    private Map<Long, Integer> expectedByProduct(List<AdminSourceTransferItem> transferItems) {
        Map<Long, Integer> expectedByProduct = new HashMap<>();
        for (AdminSourceTransferItem transferItem : transferItems) {
            if (transferItem.getProduct() == null) {
                continue;
            }
            expectedByProduct.put(transferItem.getProduct().getId(), transferItem.getQuantity());
        }
        return expectedByProduct;
    }

    private Map<Long, Integer> toReceivedByProduct(
        List<AdminSourceTransferItem> transferItems,
        Map<Long, Integer> missingByProduct
    ) {
        Map<Long, Integer> receivedByProduct = new HashMap<>();
        for (AdminSourceTransferItem transferItem : transferItems) {
            if (transferItem.getProduct() == null) {
                continue;
            }
            Long productId = transferItem.getProduct().getId();
            int expected = transferItem.getQuantity();
            int missing = missingByProduct.getOrDefault(productId, 0);
            receivedByProduct.put(productId, Math.max(0, expected - missing));
        }
        return receivedByProduct;
    }

    private Map<Long, Integer> toReturnedByProduct(
        List<AdminSourceTransferItem> transferItems,
        Map<Long, Integer> missingByProduct
    ) {
        Map<Long, Integer> returnedByProduct = new HashMap<>();
        for (AdminSourceTransferItem transferItem : transferItems) {
            if (transferItem.getProduct() == null) {
                continue;
            }
            Long productId = transferItem.getProduct().getId();
            int expected = transferItem.getQuantity();
            int missing = missingByProduct.getOrDefault(productId, 0);
            returnedByProduct.put(productId, Math.max(0, expected - missing));
        }
        return returnedByProduct;
    }

    private void recordIncident(
        AdminSourceTransfer transfer,
        List<AdminSourceTransferItem> transferItems,
        Map<Long, Integer> missingByProduct,
        AdminSourceTransferIncidentType incidentType,
        String description,
        Long currentUserId
    ) {
        AdminSourceTransferIncident incident = new AdminSourceTransferIncident();
        incident.setTransfer(transfer);
        incident.setIncidentType(incidentType);
        incident.setDescription(description);
        incident.setCreatedByAdminUserId(currentUserId);
        AdminSourceTransferIncident savedIncident = transferIncidentRepository.save(incident);

        List<AdminSourceTransferIncidentItem> incidentItems = transferItems.stream()
            .map(item -> {
                AdminSourceTransferIncidentItem incidentItem = new AdminSourceTransferIncidentItem();
                incidentItem.setIncident(savedIncident);
                incidentItem.setProduct(item.getProduct());
                incidentItem.setProductName(item.getProductName());
                int expected = item.getQuantity();
                int missing = item.getProduct() == null
                    ? 0
                    : missingByProduct.getOrDefault(item.getProduct().getId(), 0);
                incidentItem.setExpectedQuantity(expected);
                incidentItem.setMissingQuantity(missing);
                incidentItem.setReceivedQuantity(Math.max(0, expected - missing));
                return incidentItem;
            })
            .toList();

        transferIncidentItemRepository.saveAll(incidentItems);
    }

    private AdminSourceTransferItem transferItemFromForm(SourceTransferItem form, AdminSourceTransfer transfer, ProductSource productSource) {
        if (productSource == null) {
            throw new SourceTransferProductSourceNotFoundException(form.getProductId(), transfer.getSourceFrom().getId());
        }
        var item = new AdminSourceTransferItem();
        item.setNote(form.getDescription());
        item.setProduct(productSource.getProduct());
        item.setProductName(productSource.getProduct().getName());
        item.setQuantity(form.getAmount());
        item.setSku(productSource.getProduct().getSku());
        item.setTransfer(transfer);

        return item;
    }

    private String generateReferenceCode() {
        return "TRF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void validateTransferBelongsToSource(AdminSourceTransfer transfer, Long sourceId) {
        if (transfer.getSourceFrom() == null || !sourceId.equals(transfer.getSourceFrom().getId())) {
            throw new SourceTransferSourceMismatchException(transfer.getId(), sourceId);
        }
    }

    private void validateTransferInvolvedSource(AdminSourceTransfer transfer, Long sourceId) {
        boolean matchesFrom = transfer.getSourceFrom() != null && sourceId.equals(transfer.getSourceFrom().getId());
        boolean matchesTo = transfer.getSourceTo() != null && sourceId.equals(transfer.getSourceTo().getId());
        if (!matchesFrom && !matchesTo) {
            throw new SourceTransferSourceMismatchException(transfer.getId(), sourceId);
        }
    }

    private void validateIncidentBelongsToSource(AdminSourceTransferIncidentItem incidentItem, Long sourceId) {
        AdminSourceTransfer transfer = incidentItem.getIncident().getTransfer();
        validateTransferInvolvedSource(transfer, sourceId);
    }

    private void recordStatusTransition(
        AdminSourceTransfer transfer,
        AdminSourceTransferStatus fromStatus,
        AdminSourceTransferStatus toStatus,
        Long changedByAdminUserId,
        Instant changedAt,
        String note
    ) {
        AdminSourceTransferStatusHistory history = new AdminSourceTransferStatusHistory();
        history.setTransfer(transfer);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setChangedByAdminUserId(changedByAdminUserId);
        history.setChangedAt(changedAt == null ? Instant.now() : changedAt);
        history.setNote(note);
        transferStatusHistoryRepository.save(history);
    }
}
