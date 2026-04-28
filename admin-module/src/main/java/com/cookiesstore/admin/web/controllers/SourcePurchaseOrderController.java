package com.cookiesstore.admin.web.controllers;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cookiesstore.admin.service.sources.SourcePurchaseOrderService;
import com.cookiesstore.admin.service.sources.SourceService;
import com.cookiesstore.admin.web.dto.sources.CreateSourcePurchaseOrder;
import com.cookiesstore.admin.web.dto.sources.PurchaseOrderItemForm;
import com.cookiesstore.admin.web.dto.sources.ResolvePurchaseOrderAdhocProductsForm;
import com.cookiesstore.common.entities.AdminSourcePurchaseOrderStatus;
import com.cookiesstore.common.repositories.AdminSourcePurchaseOrderItemRepository;
import com.cookiesstore.common.repositories.AdminSourcePurchaseOrderRepository;
import com.cookiesstore.common.repositories.ProductRepository;

import jakarta.validation.Valid;

@Controller
public class SourcePurchaseOrderController {

    private final SourceService sourceService;
    private final SourcePurchaseOrderService sourcePurchaseOrderService;
    private final ProductRepository productRepository;
    private final AdminSourcePurchaseOrderRepository purchaseOrderRepository;
    private final AdminSourcePurchaseOrderItemRepository purchaseOrderItemRepository;

    public SourcePurchaseOrderController(
        SourceService sourceService,
        SourcePurchaseOrderService sourcePurchaseOrderService,
        ProductRepository productRepository,
        AdminSourcePurchaseOrderRepository purchaseOrderRepository,
        AdminSourcePurchaseOrderItemRepository purchaseOrderItemRepository
    ) {
        this.sourceService = sourceService;
        this.sourcePurchaseOrderService = sourcePurchaseOrderService;
        this.productRepository = productRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
    }

    @GetMapping(
        value = "/admin/product-sources/{sourceId}/manage/purchase-orders",
        produces = "text/html",
        name = "admin.product-sources.manage.purchase-orders.list"
    )
    public String listPurchaseOrders(
        @PathVariable("sourceId") Long sourceId,
        @RequestParam(value = "poStatus", required = false) String poStatus,
        Model model
    ) {
        model.addAttribute("source", sourceService.getSource(sourceId));
        model.addAttribute("manageSection", "purchase-orders");
        populatePurchaseOrdersSection(model, sourceId, normalizePurchaseOrderStatus(poStatus));
        return "backoffice/product-sources/manage";
    }

    @GetMapping(
        value = "/admin/product-sources/{sourceId}/manage/purchase-orders/new",
        produces = "text/html",
        name = "admin.product-sources.manage.purchase-orders.new.view"
    )
    public String newPurchaseOrderForm(
        @PathVariable("sourceId") Long sourceId,
        Model model
    ) {
        populateFormOptions(model, sourceId, false, null);
        if (!model.containsAttribute("form")) {
            CreateSourcePurchaseOrder form = new CreateSourcePurchaseOrder();
            form.setItems(java.util.List.of(new PurchaseOrderItemForm()));
            model.addAttribute("form", form);
        }
        return "backoffice/product-sources/manage/purchase_orders_form";
    }

    @PostMapping(
        value = "/admin/product-sources/{sourceId}/manage/purchase-orders/create",
        produces = "text/html",
        name = "admin.product-sources.manage.purchase-orders.new.create"
    )
    public String createPurchaseOrder(
        @PathVariable("sourceId") Long sourceId,
        @Valid @ModelAttribute("form") CreateSourcePurchaseOrder form,
        BindingResult bindingResult,
        @ModelAttribute("currentUserId") Long currentUserId,
        Model model
    ) {
        if (bindingResult.hasErrors()) {
            populateFormOptions(model, sourceId, false, null);
            return "backoffice/product-sources/manage/purchase_orders_form";
        }

        var purchaseOrder = sourcePurchaseOrderService.createDraft(sourceId, form, currentUserId);
        return "redirect:/admin/product-sources/" + sourceId + "/manage/purchase-orders/" + purchaseOrder.getId() + "/edit";
    }

    @GetMapping(
        value = "/admin/product-sources/{sourceId}/manage/purchase-orders/{purchaseOrderId}/edit",
        produces = "text/html",
        name = "admin.product-sources.manage.purchase-orders.edit.view"
    )
    public String editPurchaseOrderForm(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("purchaseOrderId") Long purchaseOrderId,
        Model model
    ) {
        populateFormOptions(model, sourceId, true, purchaseOrderId);
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", sourcePurchaseOrderService.buildEditForm(sourceId, purchaseOrderId));
        }
        model.addAttribute("purchaseOrderId", purchaseOrderId);
        return "backoffice/product-sources/manage/purchase_orders_form";
    }

    @PostMapping(
        value = "/admin/product-sources/{sourceId}/manage/purchase-orders/{purchaseOrderId}/update",
        produces = "text/html",
        name = "admin.product-sources.manage.purchase-orders.edit.update"
    )
    public String updatePurchaseOrder(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("purchaseOrderId") Long purchaseOrderId,
        @Valid @ModelAttribute("form") CreateSourcePurchaseOrder form,
        BindingResult bindingResult,
        @ModelAttribute("currentUserId") Long currentUserId,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            populateFormOptions(model, sourceId, true, purchaseOrderId);
            return "backoffice/product-sources/manage/purchase_orders_form";
        }

        sourcePurchaseOrderService.updateDraft(sourceId, purchaseOrderId, form, currentUserId);
        redirectAttributes.addFlashAttribute("successMessage", "Purchase order draft updated.");
        return "redirect:/admin/product-sources/" + sourceId + "/manage/purchase-orders/" + purchaseOrderId + "/edit";
    }

    @PostMapping(
        value = "/admin/product-sources/{sourceId}/manage/purchase-orders/{purchaseOrderId}/cancel",
        produces = "text/html",
        name = "admin.product-sources.manage.purchase-orders.cancel"
    )
    public String cancelPurchaseOrder(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("purchaseOrderId") Long purchaseOrderId,
        @ModelAttribute("currentUserId") Long currentUserId,
        RedirectAttributes redirectAttributes
    ) {
        sourcePurchaseOrderService.cancelPurchaseOrder(sourceId, purchaseOrderId, currentUserId);
        redirectAttributes.addFlashAttribute("successMessage", "Purchase order canceled.");
        return "redirect:/admin/product-sources/" + sourceId + "/manage/purchase-orders";
    }

    @PostMapping(
        value = "/admin/product-sources/{sourceId}/manage/purchase-orders/{purchaseOrderId}/submit",
        produces = "text/html",
        name = "admin.product-sources.manage.purchase-orders.submit"
    )
    public String submitPurchaseOrder(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("purchaseOrderId") Long purchaseOrderId,
        @ModelAttribute("currentUserId") Long currentUserId,
        RedirectAttributes redirectAttributes
    ) {
        sourcePurchaseOrderService.submitPurchaseOrder(sourceId, purchaseOrderId, currentUserId);
        redirectAttributes.addFlashAttribute("successMessage", "Purchase order submitted.");
        return "redirect:/admin/product-sources/" + sourceId + "/manage/purchase-orders";
    }

    @PostMapping(
        value = "/admin/product-sources/{sourceId}/manage/purchase-orders/{purchaseOrderId}/partial-receive",
        produces = "text/html",
        name = "admin.product-sources.manage.purchase-orders.partial-receive"
    )
    public String markPartiallyReceived(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("purchaseOrderId") Long purchaseOrderId,
        @ModelAttribute("currentUserId") Long currentUserId,
        RedirectAttributes redirectAttributes
    ) {
        sourcePurchaseOrderService.markPartiallyReceived(sourceId, purchaseOrderId, currentUserId);
        redirectAttributes.addFlashAttribute("successMessage", "Purchase order marked as PARTIALLY_RECEIVED.");
        return "redirect:/admin/product-sources/" + sourceId + "/manage/purchase-orders";
    }

    @GetMapping(
        value = "/admin/product-sources/{sourceId}/manage/purchase-orders/{purchaseOrderId}/resolve-adhoc",
        produces = "text/html",
        name = "admin.product-sources.manage.purchase-orders.resolve-adhoc.view"
    )
    public String resolveAdhocProductsForm(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("purchaseOrderId") Long purchaseOrderId,
        Model model
    ) {
        populateResolveAdhocFormOptions(model, sourceId, purchaseOrderId);
        if (!(model.asMap().get("form") instanceof ResolvePurchaseOrderAdhocProductsForm)) {
            model.addAttribute("form", sourcePurchaseOrderService.buildAdhocResolutionForm(sourceId, purchaseOrderId));
        }
        return "backoffice/product-sources/manage/purchase_orders_resolve_adhoc";
    }

    @PostMapping(
        value = "/admin/product-sources/{sourceId}/manage/purchase-orders/{purchaseOrderId}/resolve-adhoc",
        produces = "text/html",
        name = "admin.product-sources.manage.purchase-orders.resolve-adhoc"
    )
    public String resolveAdhocProducts(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("purchaseOrderId") Long purchaseOrderId,
        @Valid @ModelAttribute("form") ResolvePurchaseOrderAdhocProductsForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            populateResolveAdhocFormOptions(model, sourceId, purchaseOrderId);
            return "backoffice/product-sources/manage/purchase_orders_resolve_adhoc";
        }

        int createdCount = sourcePurchaseOrderService.createAdhocProductsForPartiallyReceivedOrder(sourceId, purchaseOrderId, form);
        if (createdCount <= 0) {
            redirectAttributes.addFlashAttribute("warningMessage", "No pending ADHOC items to create.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", createdCount + " ADHOC product(s) created and linked.");
        }
        return "redirect:/admin/product-sources/" + sourceId + "/manage/purchase-orders";
    }

    @PostMapping(
        value = "/admin/product-sources/{sourceId}/manage/purchase-orders/{purchaseOrderId}/receive",
        produces = "text/html",
        name = "admin.product-sources.manage.purchase-orders.receive"
    )
    public String receivePurchaseOrder(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("purchaseOrderId") Long purchaseOrderId,
        @ModelAttribute("currentUserId") Long currentUserId,
        RedirectAttributes redirectAttributes
    ) {
        var purchaseOrder = sourcePurchaseOrderService.receivePurchaseOrder(sourceId, purchaseOrderId, currentUserId);
        if (purchaseOrder.getStatus() == AdminSourcePurchaseOrderStatus.PARTIALLY_RECEIVED) {
            redirectAttributes.addFlashAttribute(
                "warningMessage",
                "Purchase order is now PARTIALLY_RECEIVED. Create/link products for pending ADHOC items to complete receipt."
            );
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Purchase order received and stock updated.");
        }
        return "redirect:/admin/product-sources/" + sourceId + "/manage/purchase-orders";
    }

    private void populateFormOptions(Model model, Long sourceId, boolean isEdit, Long purchaseOrderId) {
        model.addAttribute("source", sourceService.getSource(sourceId));
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("catalogProducts", productRepository.findByActiveTrueAndListableTrueOrderByNameAsc());
        if (isEdit && purchaseOrderId != null) {
            model.addAttribute(
                "formAction",
                "/admin/product-sources/" + sourceId + "/manage/purchase-orders/" + purchaseOrderId + "/update"
            );
            model.addAttribute("purchaseOrderId", purchaseOrderId);
        } else {
            model.addAttribute("formAction", "/admin/product-sources/" + sourceId + "/manage/purchase-orders/create");
        }
    }

    private void populateResolveAdhocFormOptions(Model model, Long sourceId, Long purchaseOrderId) {
        model.addAttribute("source", sourceService.getSource(sourceId));
        model.addAttribute("purchaseOrder", sourcePurchaseOrderService.findForAdhocResolution(sourceId, purchaseOrderId));
        model.addAttribute("activeCategories", sourcePurchaseOrderService.listActiveCategoriesForAdhocResolution());
        model.addAttribute(
            "resolveAdhocAction",
            "/admin/product-sources/" + sourceId + "/manage/purchase-orders/" + purchaseOrderId + "/resolve-adhoc"
        );
    }

    private void populatePurchaseOrdersSection(
        Model model,
        Long sourceId,
        AdminSourcePurchaseOrderStatus filterStatus
    ) {
        var purchaseOrders = filterStatus == null
            ? purchaseOrderRepository.findBySourceIdOrderByCreatedAtDesc(sourceId)
            : purchaseOrderRepository.findBySourceIdAndStatusOrderByCreatedAtDesc(sourceId, filterStatus);

        Map<Long, Long> totalsMinorByOrderId = new LinkedHashMap<>();
        if (!purchaseOrders.isEmpty()) {
            var orderIds = purchaseOrders.stream().map(order -> order.getId()).toList();
            totalsMinorByOrderId.putAll(
                purchaseOrderItemRepository.sumTotalsByPurchaseOrderIds(orderIds).stream()
                    .collect(
                        java.util.stream.Collectors.toMap(
                            AdminSourcePurchaseOrderItemRepository.PurchaseOrderTotalProjection::getPurchaseOrderId,
                            AdminSourcePurchaseOrderItemRepository.PurchaseOrderTotalProjection::getTotalMinor
                        )
                    )
            );
        }

        Map<String, Long> statusCounts = new LinkedHashMap<>();
        statusCounts.put("ALL", purchaseOrderRepository.countBySourceId(sourceId));
        Arrays.stream(AdminSourcePurchaseOrderStatus.values()).forEach(status -> statusCounts.put(
            status.name(),
            purchaseOrderRepository.countBySourceIdAndStatus(sourceId, status)
        ));

        model.addAttribute("purchaseOrders", purchaseOrders);
        model.addAttribute("purchaseOrderTotalsMinorById", totalsMinorByOrderId);
        model.addAttribute("purchaseOrderStatusFilter", filterStatus == null ? "ALL" : filterStatus.name());
        model.addAttribute("purchaseOrderStatusCounts", statusCounts);
        model.addAttribute("purchaseOrderStatuses", List.of(
            AdminSourcePurchaseOrderStatus.DRAFT,
            AdminSourcePurchaseOrderStatus.SUBMITTED,
            AdminSourcePurchaseOrderStatus.PARTIALLY_RECEIVED,
            AdminSourcePurchaseOrderStatus.RECEIVED,
            AdminSourcePurchaseOrderStatus.CANCELED
        ));
    }

    private AdminSourcePurchaseOrderStatus normalizePurchaseOrderStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank() || "ALL".equalsIgnoreCase(rawStatus.trim())) {
            return null;
        }
        try {
            return AdminSourcePurchaseOrderStatus.valueOf(rawStatus.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
