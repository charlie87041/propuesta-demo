package com.cookiesstore.admin.service.sources;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.cookiesstore.admin.web.dto.sources.CreateSourcePurchaseOrder;
import com.cookiesstore.admin.web.dto.sources.PurchaseOrderItemForm;
import com.cookiesstore.admin.web.dto.sources.ResolvePurchaseOrderAdhocItemForm;
import com.cookiesstore.admin.web.dto.sources.ResolvePurchaseOrderAdhocProductsForm;
import com.cookiesstore.admin.web.dto.products.CreateProductForm;
import com.cookiesstore.admin.web.dto.products.UpdateProductForm;
import com.cookiesstore.common.entities.AdminSourcePurchaseOrder;
import com.cookiesstore.common.entities.AdminSourcePurchaseOrderItem;
import com.cookiesstore.common.entities.AdminSourcePurchaseOrderItemType;
import com.cookiesstore.common.entities.AdminSourcePurchaseOrderStatus;
import com.cookiesstore.common.entities.AdminSourcePurchaseOrderStatusHistory;
import com.cookiesstore.common.entities.Category;
import com.cookiesstore.common.entities.Product;
import com.cookiesstore.common.entities.Source;
import com.cookiesstore.common.repositories.AdminSourcePurchaseOrderItemRepository;
import com.cookiesstore.common.repositories.AdminSourcePurchaseOrderRepository;
import com.cookiesstore.common.repositories.AdminSourcePurchaseOrderStatusHistoryRepository;
import com.cookiesstore.common.repositories.CategoryRepository;
import com.cookiesstore.common.repositories.ProductRepository;
import com.cookiesstore.common.repositories.SourceRepository;
import com.cookiesstore.common.services.stock.StockMovementService;
import com.cookiesstore.common.services.products.ProductService;

import jakarta.transaction.Transactional;

@Service
public class SourcePurchaseOrderService {
    private static final EnumSet<AdminSourcePurchaseOrderStatus> CANCELABLE_STATUSES = EnumSet.of(
        AdminSourcePurchaseOrderStatus.DRAFT,
        AdminSourcePurchaseOrderStatus.SUBMITTED
    );
    private static final EnumSet<AdminSourcePurchaseOrderStatus> RECEIVABLE_STATUSES = EnumSet.of(
        AdminSourcePurchaseOrderStatus.SUBMITTED,
        AdminSourcePurchaseOrderStatus.PARTIALLY_RECEIVED
    );

    private final AdminSourcePurchaseOrderRepository purchaseOrderRepository;
    private final AdminSourcePurchaseOrderItemRepository purchaseOrderItemRepository;
    private final AdminSourcePurchaseOrderStatusHistoryRepository purchaseOrderStatusHistoryRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SourceRepository sourceRepository;
    private final StockMovementService stockMovementService;
    private final ProductService<CreateProductForm, UpdateProductForm> productService;

    public SourcePurchaseOrderService(
        AdminSourcePurchaseOrderRepository purchaseOrderRepository,
        AdminSourcePurchaseOrderItemRepository purchaseOrderItemRepository,
        AdminSourcePurchaseOrderStatusHistoryRepository purchaseOrderStatusHistoryRepository,
        ProductRepository productRepository,
        CategoryRepository categoryRepository,
        SourceRepository sourceRepository,
        StockMovementService stockMovementService,
        ProductService<CreateProductForm, UpdateProductForm> productService
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.purchaseOrderStatusHistoryRepository = purchaseOrderStatusHistoryRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.sourceRepository = sourceRepository;
        this.stockMovementService = stockMovementService;
        this.productService = productService;
    }

    @Transactional
    public AdminSourcePurchaseOrder createDraft(
        Long sourceId,
        CreateSourcePurchaseOrder form,
        Long currentUserId
    ) {
        Source source = sourceRepository.findById(sourceId)
            .orElseThrow(() -> new SourceNotFoundException(sourceId));

        AdminSourcePurchaseOrder purchaseOrder = new AdminSourcePurchaseOrder();
        purchaseOrder.setSource(source);
        purchaseOrder.setSupplierName(form.getSupplierName());
        purchaseOrder.setSupplierReference(form.getSupplierReference());
        purchaseOrder.setExpectedAt(form.getExpectedDeliveryDate().atStartOfDay().toInstant(ZoneOffset.UTC));
        purchaseOrder.setNotes(form.getNotes());
        purchaseOrder.setStatus(AdminSourcePurchaseOrderStatus.DRAFT);
        purchaseOrder.setCreatedByAdminUserId(currentUserId);
        purchaseOrder.setUpdatedByAdminUserId(currentUserId);
        purchaseOrder.setTotalItems(form.getItems().size());
        purchaseOrder.setTotalQuantity(sumTotalQuantity(form.getItems()));

        AdminSourcePurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        purchaseOrderItemRepository.saveAll(toItemEntities(saved, form.getItems()));
        recordStatusTransition(saved, null, AdminSourcePurchaseOrderStatus.DRAFT, currentUserId, null);
        return saved;
    }

    @Transactional
    public AdminSourcePurchaseOrder updateDraft(
        Long sourceId,
        Long purchaseOrderId,
        CreateSourcePurchaseOrder form,
        Long currentUserId
    ) {
        AdminSourcePurchaseOrder purchaseOrder = purchaseOrderRepository.findForUpdateById(purchaseOrderId)
            .orElseThrow(() -> new SourcePurchaseOrderNotFoundException(purchaseOrderId));

        validateBelongsToSource(purchaseOrder, sourceId);
        ensureDraftStatus(purchaseOrder);

        purchaseOrder.setSupplierName(form.getSupplierName());
        purchaseOrder.setSupplierReference(form.getSupplierReference());
        purchaseOrder.setExpectedAt(form.getExpectedDeliveryDate().atStartOfDay().toInstant(ZoneOffset.UTC));
        purchaseOrder.setNotes(form.getNotes());
        purchaseOrder.setUpdatedByAdminUserId(currentUserId);
        purchaseOrder.setTotalItems(form.getItems().size());
        purchaseOrder.setTotalQuantity(sumTotalQuantity(form.getItems()));

        AdminSourcePurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        purchaseOrderItemRepository.deleteByPurchaseOrderId(saved.getId());
        purchaseOrderItemRepository.saveAll(toItemEntities(saved, form.getItems()));
        return saved;
    }

    @Transactional
    public AdminSourcePurchaseOrder findByIdForDraftEdit(Long sourceId, Long purchaseOrderId) {
        AdminSourcePurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
            .orElseThrow(() -> new SourcePurchaseOrderNotFoundException(purchaseOrderId));
        validateBelongsToSource(purchaseOrder, sourceId);
        ensureDraftStatus(purchaseOrder);
        return purchaseOrder;
    }

    @Transactional
    public CreateSourcePurchaseOrder buildEditForm(Long sourceId, Long purchaseOrderId) {
        AdminSourcePurchaseOrder purchaseOrder = findByIdForDraftEdit(sourceId, purchaseOrderId);
        List<AdminSourcePurchaseOrderItem> items = purchaseOrderItemRepository.findByPurchaseOrderId(purchaseOrder.getId());

        CreateSourcePurchaseOrder form = new CreateSourcePurchaseOrder();
        form.setSupplierName(purchaseOrder.getSupplierName());
        form.setSupplierReference(purchaseOrder.getSupplierReference());
        if (purchaseOrder.getExpectedAt() != null) {
            form.setExpectedDeliveryDate(purchaseOrder.getExpectedAt().atZone(ZoneOffset.UTC).toLocalDate());
        }
        form.setNotes(purchaseOrder.getNotes());
        List<PurchaseOrderItemForm> formItems = items.stream().map(this::toItemForm).toList();
        if (formItems.isEmpty()) {
            formItems = List.of(new PurchaseOrderItemForm());
        }
        form.setItems(formItems);
        return form;
    }

    @Transactional
    public AdminSourcePurchaseOrder cancelPurchaseOrder(
        Long sourceId,
        Long purchaseOrderId,
        Long currentUserId
    ) {
        AdminSourcePurchaseOrder purchaseOrder = purchaseOrderRepository.findForUpdateById(purchaseOrderId)
            .orElseThrow(() -> new SourcePurchaseOrderNotFoundException(purchaseOrderId));
        validateBelongsToSource(purchaseOrder, sourceId);

        AdminSourcePurchaseOrderStatus previousStatus = purchaseOrder.getStatus();
        if (!CANCELABLE_STATUSES.contains(previousStatus)) {
            throw new SourcePurchaseOrderCancelableStatusRequiredException(purchaseOrderId);
        }

        purchaseOrder.setStatus(AdminSourcePurchaseOrderStatus.CANCELED);
        purchaseOrder.setUpdatedByAdminUserId(currentUserId);
        AdminSourcePurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        recordStatusTransition(saved, previousStatus, AdminSourcePurchaseOrderStatus.CANCELED, currentUserId, null);
        return saved;
    }

    @Transactional
    public AdminSourcePurchaseOrder submitPurchaseOrder(
        Long sourceId,
        Long purchaseOrderId,
        Long currentUserId
    ) {
        AdminSourcePurchaseOrder purchaseOrder = purchaseOrderRepository.findForUpdateById(purchaseOrderId)
            .orElseThrow(() -> new SourcePurchaseOrderNotFoundException(purchaseOrderId));
        validateBelongsToSource(purchaseOrder, sourceId);

        AdminSourcePurchaseOrderStatus previousStatus = purchaseOrder.getStatus();
        if (previousStatus != AdminSourcePurchaseOrderStatus.DRAFT) {
            throw new SourcePurchaseOrderSubmittableStatusRequiredException(purchaseOrderId);
        }

        purchaseOrder.setStatus(AdminSourcePurchaseOrderStatus.SUBMITTED);
        purchaseOrder.setPlacedAt(Instant.now());
        purchaseOrder.setUpdatedByAdminUserId(currentUserId);
        AdminSourcePurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        recordStatusTransition(saved, previousStatus, AdminSourcePurchaseOrderStatus.SUBMITTED, currentUserId, null);
        return saved;
    }

    @Transactional
    public AdminSourcePurchaseOrder receivePurchaseOrder(
        Long sourceId,
        Long purchaseOrderId,
        Long currentUserId
    ) {
        AdminSourcePurchaseOrder purchaseOrder = purchaseOrderRepository.findForUpdateById(purchaseOrderId)
            .orElseThrow(() -> new SourcePurchaseOrderNotFoundException(purchaseOrderId));
        validateBelongsToSource(purchaseOrder, sourceId);

        AdminSourcePurchaseOrderStatus previousStatus = purchaseOrder.getStatus();
        if (!RECEIVABLE_STATUSES.contains(previousStatus)) {
            throw new SourcePurchaseOrderReceivableStatusRequiredException(purchaseOrderId);
        }

        List<AdminSourcePurchaseOrderItem> items = purchaseOrderItemRepository.findByPurchaseOrderId(purchaseOrder.getId());
        String referenceCode = resolvePurchaseOrderReferenceCode(purchaseOrder);
        Instant receivedAt = Instant.now();

        boolean hasPendingItemsWithoutLinkedProduct = false;
        for (AdminSourcePurchaseOrderItem item : items) {
            int orderedQty = item.getOrderedQty() == null ? 0 : item.getOrderedQty();
            int receivedQty = item.getReceivedQty() == null ? 0 : item.getReceivedQty();
            int missingQty = Math.max(orderedQty - receivedQty, 0);
            if (missingQty <= 0) {
                continue;
            }
            if (item.getProduct() == null) {
                hasPendingItemsWithoutLinkedProduct = true;
                continue;
            }

            stockMovementService.registerPurchaseReceipt(
                purchaseOrder.getId(),
                sourceId,
                item.getProduct().getId(),
                missingQty,
                item.getUnitCostMinor(),
                receivedAt,
                currentUserId,
                item.getNote(),
                referenceCode
            );
            item.setReceivedQty(orderedQty);
        }
        purchaseOrderItemRepository.saveAll(items);

        if (hasPendingItemsWithoutLinkedProduct) {
            if (previousStatus == AdminSourcePurchaseOrderStatus.SUBMITTED) {
                purchaseOrder.setStatus(AdminSourcePurchaseOrderStatus.PARTIALLY_RECEIVED);
                purchaseOrder.setUpdatedByAdminUserId(currentUserId);
                AdminSourcePurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
                recordStatusTransition(
                    saved,
                    previousStatus,
                    AdminSourcePurchaseOrderStatus.PARTIALLY_RECEIVED,
                    currentUserId,
                    "Pending items require catalog product creation before full receipt."
                );
                return saved;
            }
            throw new SourcePurchaseOrderReceiptProductRequiredException(purchaseOrderId);
        }

        purchaseOrder.setStatus(AdminSourcePurchaseOrderStatus.RECEIVED);
        purchaseOrder.setReceivedAt(receivedAt);
        purchaseOrder.setUpdatedByAdminUserId(currentUserId);
        AdminSourcePurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        recordStatusTransition(saved, previousStatus, AdminSourcePurchaseOrderStatus.RECEIVED, currentUserId, null);
        return saved;
    }

    @Transactional
    public AdminSourcePurchaseOrder markPartiallyReceived(
        Long sourceId,
        Long purchaseOrderId,
        Long currentUserId
    ) {
        AdminSourcePurchaseOrder purchaseOrder = purchaseOrderRepository.findForUpdateById(purchaseOrderId)
            .orElseThrow(() -> new SourcePurchaseOrderNotFoundException(purchaseOrderId));
        validateBelongsToSource(purchaseOrder, sourceId);

        AdminSourcePurchaseOrderStatus previousStatus = purchaseOrder.getStatus();
        if (previousStatus != AdminSourcePurchaseOrderStatus.SUBMITTED) {
            throw new SourcePurchaseOrderPartiallyReceivableStatusRequiredException(purchaseOrderId);
        }

        purchaseOrder.setStatus(AdminSourcePurchaseOrderStatus.PARTIALLY_RECEIVED);
        purchaseOrder.setUpdatedByAdminUserId(currentUserId);
        AdminSourcePurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        recordStatusTransition(saved, previousStatus, AdminSourcePurchaseOrderStatus.PARTIALLY_RECEIVED, currentUserId, null);
        return saved;
    }

    @Transactional
    public int createAdhocProductsForPartiallyReceivedOrder(
        Long sourceId,
        Long purchaseOrderId,
        ResolvePurchaseOrderAdhocProductsForm form
    ) {
        AdminSourcePurchaseOrder purchaseOrder = purchaseOrderRepository.findForUpdateById(purchaseOrderId)
            .orElseThrow(() -> new SourcePurchaseOrderNotFoundException(purchaseOrderId));
        validateBelongsToSource(purchaseOrder, sourceId);
        if (purchaseOrder.getStatus() != AdminSourcePurchaseOrderStatus.PARTIALLY_RECEIVED) {
            throw new SourcePurchaseOrderAdhocResolutionStatusRequiredException(purchaseOrderId);
        }

        List<Category> activeCategories = categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc();
        Category defaultCategory = activeCategories.stream()
            .findFirst()
            .orElseThrow(SourcePurchaseOrderNoActiveCategoryForAdhocException::new);
        List<Long> activeCategoryIds = activeCategories.stream().map(Category::getId).toList();

        List<AdminSourcePurchaseOrderItem> items = listPendingAdhocItemsForResolution(sourceId, purchaseOrderId);
        if (items.isEmpty()) {
            return 0;
        }
        var pendingItemsById = items.stream().collect(Collectors.toMap(AdminSourcePurchaseOrderItem::getId, item -> item));

        int createdCount = 0;
        for (ResolvePurchaseOrderAdhocItemForm formItem : form.getItems()) {
            AdminSourcePurchaseOrderItem item = pendingItemsById.get(formItem.getPurchaseOrderItemId());
            if (item == null) {
                continue;
            }
            String productName = StringUtils.hasText(formItem.getName())
                ? formItem.getName().trim()
                : (StringUtils.hasText(item.getProductName()) ? item.getProductName().trim() : ("PO " + purchaseOrderId + " Item " + item.getId()));
            String sku = StringUtils.hasText(formItem.getSku())
                ? formItem.getSku().trim()
                : generateUniqueSku(purchaseOrderId, item.getId());
            String slug = StringUtils.hasText(formItem.getSlug())
                ? formItem.getSlug().trim()
                : generateUniqueSlug(productName, purchaseOrderId, item.getId());
            Long categoryId = formItem.getCategoryId() != null ? formItem.getCategoryId() : defaultCategory.getId();
            if (!activeCategoryIds.contains(categoryId)) {
                throw new SourcePurchaseOrderNoActiveCategoryForAdhocException();
            }

            BigDecimal unitPrice = formItem.getUnitPrice() != null
                ? formItem.getUnitPrice().setScale(2, RoundingMode.HALF_UP)
                : toAmount(item.getUnitCostMinor());
            if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                unitPrice = BigDecimal.valueOf(0.01).setScale(2, RoundingMode.HALF_UP);
            }

            Product created = productService.createProduct(
                new CreateProductForm(
                    sku,
                    productName,
                    slug,
                    trimToNull(formItem.getDescription()),
                    categoryId,
                    trimToNull(formItem.getMainImageUrl()),
                    formItem.isActive(),
                    formItem.isVisible(),
                    List.of(sourceId),
                    0,
                    0,
                    unitPrice,
                    "SIMPLE",
                    List.of(),
                    List.of(),
                    null,
                    null,
                    null,
                    null,
                    formItem.isListable(),
                    formItem.isPurchasable(),
                    formItem.isPurchasableAlone(),
                    null,
                    List.of()
                )
            );

            item.setProduct(created);
            item.setSku(created.getSku());
            item.setProductName(created.getName());
            createdCount++;
        }
        purchaseOrderItemRepository.saveAll(items);
        return createdCount;
    }

    @Transactional
    public ResolvePurchaseOrderAdhocProductsForm buildAdhocResolutionForm(Long sourceId, Long purchaseOrderId) {
        AdminSourcePurchaseOrder purchaseOrder = findForAdhocResolution(sourceId, purchaseOrderId);
        List<Category> activeCategories = categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc();
        Category defaultCategory = activeCategories.stream()
            .findFirst()
            .orElseThrow(SourcePurchaseOrderNoActiveCategoryForAdhocException::new);

        List<ResolvePurchaseOrderAdhocItemForm> formItems = listPendingAdhocItemsForResolution(sourceId, purchaseOrderId).stream()
            .map(item -> toResolveAdhocItemForm(purchaseOrder.getId(), item, defaultCategory.getId()))
            .toList();

        ResolvePurchaseOrderAdhocProductsForm form = new ResolvePurchaseOrderAdhocProductsForm();
        form.setItems(formItems);
        return form;
    }

    @Transactional
    public List<Category> listActiveCategoriesForAdhocResolution() {
        return categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc();
    }

    @Transactional
    public AdminSourcePurchaseOrder findForAdhocResolution(Long sourceId, Long purchaseOrderId) {
        AdminSourcePurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
            .orElseThrow(() -> new SourcePurchaseOrderNotFoundException(purchaseOrderId));
        validateBelongsToSource(purchaseOrder, sourceId);
        if (purchaseOrder.getStatus() != AdminSourcePurchaseOrderStatus.PARTIALLY_RECEIVED) {
            throw new SourcePurchaseOrderAdhocResolutionStatusRequiredException(purchaseOrderId);
        }
        return purchaseOrder;
    }

    @Transactional
    public List<AdminSourcePurchaseOrderItem> listPendingAdhocItemsForResolution(Long sourceId, Long purchaseOrderId) {
        findForAdhocResolution(sourceId, purchaseOrderId);
        return purchaseOrderItemRepository.findByPurchaseOrderId(purchaseOrderId).stream()
            .filter(item -> item.getItemType() == AdminSourcePurchaseOrderItemType.AD_HOC_PRODUCT)
            .filter(item -> item.getProduct() == null)
            .collect(Collectors.toList());
    }

    private void validateBelongsToSource(AdminSourcePurchaseOrder purchaseOrder, Long sourceId) {
        if (purchaseOrder.getSource() == null || !sourceId.equals(purchaseOrder.getSource().getId())) {
            throw new SourcePurchaseOrderSourceMismatchException(purchaseOrder.getId(), sourceId);
        }
    }

    private void ensureDraftStatus(AdminSourcePurchaseOrder purchaseOrder) {
        if (purchaseOrder.getStatus() != AdminSourcePurchaseOrderStatus.DRAFT) {
            throw new SourcePurchaseOrderDraftRequiredException(purchaseOrder.getId());
        }
    }

    private List<AdminSourcePurchaseOrderItem> toItemEntities(
        AdminSourcePurchaseOrder purchaseOrder,
        List<PurchaseOrderItemForm> items
    ) {
        return items.stream()
            .map(item -> {
                AdminSourcePurchaseOrderItem entity = new AdminSourcePurchaseOrderItem();
                entity.setPurchaseOrder(purchaseOrder);
                AdminSourcePurchaseOrderItemType itemType = item.getItemType() == null
                    ? AdminSourcePurchaseOrderItemType.CATALOG_PRODUCT
                    : item.getItemType();
                entity.setItemType(itemType);

                if (itemType == AdminSourcePurchaseOrderItemType.CATALOG_PRODUCT) {
                    if (item.getProductId() == null) {
                        throw new SourcePurchaseOrderItemProductRequiredException();
                    }
                    Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new SourcePurchaseOrderItemProductNotFoundException(item.getProductId()));
                    if (!product.isActive() || !Boolean.TRUE.equals(product.getListable())) {
                        throw new SourcePurchaseOrderItemCatalogProductNotEligibleException(item.getProductId());
                    }
                    entity.setProduct(product);
                    entity.setProductName(product.getName());
                    entity.setSku(StringUtils.hasText(item.getSku()) ? item.getSku().trim() : product.getSku());
                } else {
                    if (!StringUtils.hasText(item.getProductName())) {
                        throw new SourcePurchaseOrderItemNameRequiredException();
                    }
                    entity.setProduct(null);
                    entity.setProductName(item.getProductName().trim());
                    entity.setSku(StringUtils.hasText(item.getSku()) ? item.getSku().trim() : null);
                }

                entity.setOrderedQty(item.getOrderedQty());
                entity.setReceivedQty(0);
                entity.setUnitCostMinor(toMinor(item.getUnitCost()));
                entity.setLineTotalMinor(toMinor(item.getUnitCost().multiply(BigDecimal.valueOf(item.getOrderedQty()))));
                entity.setNote(item.getNote());
                return entity;
            })
            .toList();
    }

    private PurchaseOrderItemForm toItemForm(AdminSourcePurchaseOrderItem item) {
        PurchaseOrderItemForm formItem = new PurchaseOrderItemForm();
        formItem.setItemType(
            item.getItemType() == null ? AdminSourcePurchaseOrderItemType.CATALOG_PRODUCT : item.getItemType()
        );
        formItem.setProductId(item.getProduct() == null ? null : item.getProduct().getId());
        formItem.setProductName(item.getProductName());
        formItem.setSku(item.getSku());
        formItem.setOrderedQty(item.getOrderedQty());
        formItem.setUnitCost(toAmount(item.getUnitCostMinor()));
        formItem.setNote(item.getNote());
        return formItem;
    }

    private int sumTotalQuantity(List<PurchaseOrderItemForm> items) {
        return items.stream()
            .mapToInt(item -> item.getOrderedQty() == null ? 0 : item.getOrderedQty())
            .sum();
    }

    private long toMinor(BigDecimal amount) {
        if (amount == null) {
            return 0L;
        }
        return amount
            .setScale(2, RoundingMode.HALF_UP)
            .movePointRight(2)
            .longValue();
    }

    private BigDecimal toAmount(Long minor) {
        if (minor == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(minor, 2).setScale(2, RoundingMode.HALF_UP);
    }

    private String resolvePurchaseOrderReferenceCode(AdminSourcePurchaseOrder purchaseOrder) {
        if (StringUtils.hasText(purchaseOrder.getSupplierReference())) {
            return purchaseOrder.getSupplierReference().trim();
        }
        return "PO-" + purchaseOrder.getId();
    }

    private String generateUniqueSku(Long purchaseOrderId, Long itemId) {
        String base = "PO" + purchaseOrderId + "-ADHOC-" + itemId;
        String candidate = base;
        int suffix = 1;
        while (productRepository.existsBySku(candidate)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private String generateUniqueSlug(String productName, Long purchaseOrderId, Long itemId) {
        String normalized = Normalizer.normalize(productName == null ? "" : productName, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .toLowerCase()
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-+|-+$", "");
        if (!StringUtils.hasText(normalized)) {
            normalized = "purchase-order-item";
        }
        String base = normalized + "-po-" + purchaseOrderId + "-item-" + itemId;
        String candidate = base;
        int suffix = 1;
        while (productRepository.findBySlug(candidate).isPresent()) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private ResolvePurchaseOrderAdhocItemForm toResolveAdhocItemForm(
        Long purchaseOrderId,
        AdminSourcePurchaseOrderItem item,
        Long defaultCategoryId
    ) {
        ResolvePurchaseOrderAdhocItemForm formItem = new ResolvePurchaseOrderAdhocItemForm();
        formItem.setPurchaseOrderItemId(item.getId());
        String productName = StringUtils.hasText(item.getProductName())
            ? item.getProductName().trim()
            : ("PO " + purchaseOrderId + " Item " + item.getId());
        formItem.setName(productName);
        formItem.setSku(StringUtils.hasText(item.getSku()) ? item.getSku().trim() : generateUniqueSku(purchaseOrderId, item.getId()));
        formItem.setSlug(generateUniqueSlug(productName, purchaseOrderId, item.getId()));
        formItem.setCategoryId(defaultCategoryId);
        formItem.setDescription("Auto-created from purchase order " + purchaseOrderId);
        formItem.setMainImageUrl("");
        BigDecimal unitPrice = toAmount(item.getUnitCostMinor());
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            unitPrice = BigDecimal.valueOf(0.01).setScale(2, RoundingMode.HALF_UP);
        }
        formItem.setUnitPrice(unitPrice);
        formItem.setOriginalProductName(item.getProductName());
        formItem.setOrderedQty(item.getOrderedQty());
        formItem.setPurchaseUnitCost(toAmount(item.getUnitCostMinor()));
        return formItem;
    }

    private String trimToNull(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        return raw.trim();
    }

    private void recordStatusTransition(
        AdminSourcePurchaseOrder purchaseOrder,
        AdminSourcePurchaseOrderStatus fromStatus,
        AdminSourcePurchaseOrderStatus toStatus,
        Long changedByAdminUserId,
        String note
    ) {
        AdminSourcePurchaseOrderStatusHistory history = new AdminSourcePurchaseOrderStatusHistory();
        history.setPurchaseOrder(purchaseOrder);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setChangedAt(Instant.now());
        history.setChangedByAdminUserId(changedByAdminUserId);
        history.setNote(note);
        purchaseOrderStatusHistoryRepository.save(history);
    }
}
