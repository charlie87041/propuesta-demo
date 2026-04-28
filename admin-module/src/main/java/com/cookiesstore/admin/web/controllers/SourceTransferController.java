package com.cookiesstore.admin.web.controllers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;

import com.cookiesstore.admin.service.sources.SourceService;
import com.cookiesstore.admin.service.sources.SourceTransferService;
import com.cookiesstore.admin.web.dto.sources.CreateSourceTransfer;
import com.cookiesstore.admin.web.dto.sources.TransferIncidentActionForm;
import com.cookiesstore.common.entities.AdminSourceTransferStatus;
import com.cookiesstore.common.entities.ProductSourceStatus;
import com.cookiesstore.common.entities.Source;
import com.cookiesstore.common.repositories.AdminSourceTransferIncidentItemRepository;
import com.cookiesstore.common.repositories.AdminSourceTransferItemRepository;
import com.cookiesstore.common.repositories.AdminSourceTransferRepository;
import com.cookiesstore.common.repositories.ProductSourceRepository;
import com.cookiesstore.common.repositories.SourceRepository;

import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.LinkedHashMap;

@Controller
public class SourceTransferController
{
    private final AdminSourceTransferRepository transferRepository;
    private final SourceService sourceService;
    private final SourceTransferService sourceTransferService;
    private final ProductSourceRepository productSourceRepository;
    private final SourceRepository sourceRepository;
    private final AdminSourceTransferItemRepository transferItemRepository;
    private final AdminSourceTransferIncidentItemRepository incidentItemRepository;


    public SourceTransferController(
        AdminSourceTransferRepository transferRepository,
        SourceService sourceService,
        SourceTransferService sourceTransferService,
        ProductSourceRepository productSourceRepository,
        SourceRepository sourceRepository,
        AdminSourceTransferItemRepository transferItemRepository,
        AdminSourceTransferIncidentItemRepository incidentItemRepository
    )
    {
        this.transferRepository = transferRepository;
        this.sourceService = sourceService;
        this.sourceTransferService = sourceTransferService;
        this.productSourceRepository = productSourceRepository;
        this.sourceRepository = sourceRepository;
        this.transferItemRepository = transferItemRepository;
        this.incidentItemRepository = incidentItemRepository;
    }

    @GetMapping(value = "/admin/product-sources/{sourceId}/manage/transfers", name = "admin.product-sources.manage.transfers.list")
    public String listStockTransfers(
        Model model,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)  Pageable page,
        @PathVariable("sourceId") Long sourceId,
        @RequestParam(value = "direction", defaultValue = "inbound") String direction,
        @RequestParam(value = "status", required = false) String status
    ){
        String normalizedDirection = "outbound".equalsIgnoreCase(direction) ? "outbound" : "inbound";
        AdminSourceTransferStatus statusFilter = normalizeTransferStatus(status);
        var transferPage = resolveTransferPage(sourceId, normalizedDirection, statusFilter, page);
        var inboundPreview = transferRepository.findFirstBySourceToIdOrderByUpdatedAtDesc(sourceId).orElse(null);
        var outboundPreview = transferRepository.findFirstBySourceFromIdOrderByUpdatedAtDesc(sourceId).orElse(null);
        long inboundCount = transferRepository.countBySourceToId(sourceId);
        long outboundCount = transferRepository.countBySourceFromId(sourceId);
        var transferStatusCounts = buildTransferStatusCounts(sourceId, normalizedDirection);
        Map<Long, List<com.cookiesstore.common.entities.AdminSourceTransferItem>> transferItemsByTransferId = transferPage.getContent()
            .stream()
            .collect(Collectors.toMap(
                transfer -> transfer.getId(),
                transfer -> transferItemRepository.findByTransferId(transfer.getId())
            ));

        model.addAttribute("source", sourceService.getSource(sourceId));
        model.addAttribute("direction", normalizedDirection);
        model.addAttribute("transferPage", transferPage);
        model.addAttribute("transferItemsByTransferId", transferItemsByTransferId);
        model.addAttribute("inboundPreview", inboundPreview);
        model.addAttribute("outboundPreview", outboundPreview);
        model.addAttribute("inboundCount", inboundCount);
        model.addAttribute("outboundCount", outboundCount);
        model.addAttribute("transferStatusFilter", statusFilter == null ? "ALL" : statusFilter.name());
        model.addAttribute("transferStatusCounts", transferStatusCounts);
        model.addAttribute("transferStatuses", Arrays.asList(AdminSourceTransferStatus.values()));
        model.addAttribute("manageSection", "transfers");
        return "backoffice/product-sources/manage/transfer_list";
    }

    @GetMapping(value = "/admin/product-sources/{sourceId}/manage/incidents", name = "admin.product-sources.manage.incidents.list")
    public String listTransferIncidents(
        Model model,
        @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable page,
        @PathVariable("sourceId") Long sourceId,
        @RequestParam(value = "status", required = false) String status
    ) {
        IncidentStatusFilter incidentStatusFilter = normalizeIncidentStatus(status);
        var incidentPage = resolveIncidentPage(sourceId, incidentStatusFilter, page);
        var incidentStatusCounts = buildIncidentStatusCounts(sourceId);
        model.addAttribute("source", sourceService.getSource(sourceId));
        model.addAttribute("incidentPage", incidentPage);
        model.addAttribute("incidentStatusFilter", incidentStatusFilter.name());
        model.addAttribute("incidentStatusCounts", incidentStatusCounts);
        model.addAttribute("incidentStatuses", Arrays.asList(IncidentStatusFilter.values()));
        model.addAttribute("manageSection", "incidents");
        return "backoffice/product-sources/manage/incidents_list";
    }

    @GetMapping(value = "/admin/product-sources/{sourceId}/manage/transfers/new", produces = "text/html", name = "admin.product-sources.manage.transfers.new.view")
    public String newTransferForm(
        @PathVariable("sourceId") Long sourceId,
        Model model
    ) {
        populateTransferFormOptions(model, sourceId);
        return "backoffice/product-sources/manage/transfers_form";
    }

    @PostMapping(value = "/admin/product-sources/{sourceId}/manage/transfers/create", produces = "text/html", name = "admin.product-sources.manage.transfers.new.create")
    public String postTransferForm(
        @PathVariable("sourceId") Long sourceId,
        @Valid @ModelAttribute("form") CreateSourceTransfer form,
        BindingResult bindingResult,
        @ModelAttribute("currentUserId") Long currentUserId,
        Model model
    )
    {
        if (bindingResult.hasErrors()) {
            populateTransferFormOptions(model, sourceId);
            return "backoffice/product-sources/manage/transfers_form";
        }
        this.sourceTransferService.startTransfer(form, sourceId, currentUserId);
        return "redirect:/admin/product-sources/" + sourceId + "/manage/transfers?direction=outbound";
    }

    @PostMapping(value = "/admin/product-sources/{sourceId}/manage/transfers/{transferId}/mark-in-transit", produces = "text/html")
    public String markTransferInTransit(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("transferId") Long transferId,
        @ModelAttribute("currentUserId") Long currentUserId,
        @RequestParam(value = "direction", defaultValue = "outbound") String direction,
        @RequestParam(value = "status", required = false) String status
    ) {
        this.sourceTransferService.markTransferInTransit(sourceId, transferId, currentUserId);
        return buildTransfersRedirect(sourceId, direction, status);
    }

    @PostMapping(value = "/admin/product-sources/{sourceId}/manage/transfers/{transferId}/delete", produces = "text/html")
    public String deleteTransfer(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("transferId") Long transferId,
        @RequestParam(value = "direction", defaultValue = "outbound") String direction,
        @RequestParam(value = "status", required = false) String status
    ) {
        this.sourceTransferService.deleteDraftTransfer(sourceId, transferId);
        return buildTransfersRedirect(sourceId, direction, status);
    }

    @PostMapping(value = "/admin/product-sources/{sourceId}/manage/transfers/{transferId}/cancel", produces = "text/html")
    public String cancelTransfer(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("transferId") Long transferId,
        @ModelAttribute("currentUserId") Long currentUserId,
        @RequestParam(value = "direction", defaultValue = "outbound") String direction,
        @RequestParam(value = "status", required = false) String status
    ) {
        this.sourceTransferService.cancelTransfer(sourceId, transferId, currentUserId);
        return buildTransfersRedirect(sourceId, direction, status);
    }

    @PostMapping(value = "/admin/product-sources/{sourceId}/manage/transfers/{transferId}/complete", produces = "text/html")
    public String completeTransfer(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("transferId") Long transferId,
        @ModelAttribute("currentUserId") Long currentUserId,
        @RequestParam(value = "direction", defaultValue = "outbound") String direction,
        @RequestParam(value = "status", required = false) String status
    ) {
        this.sourceTransferService.completeTransfer(sourceId, transferId, currentUserId);
        return buildTransfersRedirect(sourceId, direction, status);
    }

    @PostMapping(value = "/admin/product-sources/{sourceId}/manage/transfers/{transferId}/cancel-with-incident", produces = "text/html")
    public String cancelTransferWithIncident(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("transferId") Long transferId,
        @ModelAttribute("currentUserId") Long currentUserId,
        @RequestParam(value = "direction", defaultValue = "outbound") String direction,
        @RequestParam(value = "status", required = false) String status,
        @Valid @ModelAttribute("incidentForm") TransferIncidentActionForm incidentForm
    ) {
        this.sourceTransferService.cancelTransferWithIncident(sourceId, transferId, currentUserId, incidentForm);
        return buildTransfersRedirect(sourceId, direction, status);
    }

    @PostMapping(value = "/admin/product-sources/{sourceId}/manage/transfers/{transferId}/complete-with-incident", produces = "text/html")
    public String completeTransferWithIncident(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("transferId") Long transferId,
        @ModelAttribute("currentUserId") Long currentUserId,
        @RequestParam(value = "direction", defaultValue = "outbound") String direction,
        @RequestParam(value = "status", required = false) String status,
        @Valid @ModelAttribute("incidentForm") TransferIncidentActionForm incidentForm
    ) {
        this.sourceTransferService.completeTransferWithIncident(sourceId, transferId, currentUserId, incidentForm);
        return buildTransfersRedirect(sourceId, direction, status);
    }

    @PostMapping(value = "/admin/product-sources/{sourceId}/manage/transfers/incidents/{incidentItemId}/close", produces = "text/html")
    public String closeIncident(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("incidentItemId") Long incidentItemId,
        @ModelAttribute("currentUserId") Long currentUserId,
        @RequestParam(value = "direction", defaultValue = "outbound") String direction,
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "incidentStatus", required = false) String incidentStatus,
        @RequestParam(value = "view", required = false) String view
    ) {
        this.sourceTransferService.closeIncident(sourceId, incidentItemId, currentUserId);
        return resolveIncidentRedirectTarget(sourceId, direction, status, incidentStatus, view);
    }

    @PostMapping(value = "/admin/product-sources/{sourceId}/manage/transfers/incidents/{incidentItemId}/revert", produces = "text/html")
    public String revertIncident(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("incidentItemId") Long incidentItemId,
        @ModelAttribute("currentUserId") Long currentUserId,
        @RequestParam(value = "direction", defaultValue = "outbound") String direction,
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "incidentStatus", required = false) String incidentStatus,
        @RequestParam(value = "view", required = false) String view
    ) {
        this.sourceTransferService.revertIncident(sourceId, incidentItemId, currentUserId);
        return resolveIncidentRedirectTarget(sourceId, direction, status, incidentStatus, view);
    }

    private void populateTransferFormOptions(Model model, Long sourceId) {
        Source source = sourceService.getSource(sourceId);
        List<Source> destinationSources = sourceRepository.findAll().stream()
            .filter(Source::isActive)
            .filter(candidate -> !candidate.getId().equals(sourceId))
            .toList();
        var sourceProducts = productSourceRepository.findBySourceIdAndStatus(sourceId, ProductSourceStatus.ACTIVE).stream()
            .filter(productSource -> productSource.getProduct() != null)
            .toList();

        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new CreateSourceTransfer());
        }

        model.addAttribute("source", source);
        model.addAttribute("manageSection", "transfers");
        model.addAttribute("destinationSources", destinationSources);
        model.addAttribute("sourceProducts", sourceProducts);
    }

    private Page<com.cookiesstore.common.entities.AdminSourceTransfer> resolveTransferPage(
        Long sourceId,
        String normalizedDirection,
        AdminSourceTransferStatus statusFilter,
        Pageable page
    ) {
        if ("inbound".equals(normalizedDirection)) {
            return statusFilter == null
                ? transferRepository.findBySourceToId(sourceId, page)
                : transferRepository.findBySourceToIdAndStatus(sourceId, statusFilter, page);
        }
        return statusFilter == null
            ? transferRepository.findBySourceFromId(sourceId, page)
            : transferRepository.findBySourceFromIdAndStatus(sourceId, statusFilter, page);
    }

    private Map<String, Long> buildTransferStatusCounts(Long sourceId, String normalizedDirection) {
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        if ("inbound".equals(normalizedDirection)) {
            statusCounts.put("ALL", transferRepository.countBySourceToId(sourceId));
            Arrays.stream(AdminSourceTransferStatus.values()).forEach(status -> statusCounts.put(
                status.name(),
                transferRepository.countBySourceToIdAndStatus(sourceId, status)
            ));
            return statusCounts;
        }

        statusCounts.put("ALL", transferRepository.countBySourceFromId(sourceId));
        Arrays.stream(AdminSourceTransferStatus.values()).forEach(status -> statusCounts.put(
            status.name(),
            transferRepository.countBySourceFromIdAndStatus(sourceId, status)
        ));
        return statusCounts;
    }

    private AdminSourceTransferStatus normalizeTransferStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank() || "ALL".equalsIgnoreCase(rawStatus.trim())) {
            return null;
        }
        try {
            return AdminSourceTransferStatus.valueOf(rawStatus.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String buildTransfersRedirect(Long sourceId, String direction, String status) {
        String normalizedDirection = "outbound".equalsIgnoreCase(direction) ? "outbound" : "inbound";
        String normalizedStatus = normalizeTransferStatus(status) == null ? "ALL" : status.trim().toUpperCase();
        return "redirect:/admin/product-sources/" + sourceId + "/manage/transfers?direction="
            + normalizedDirection + "&status=" + normalizedStatus;
    }

    private Page<com.cookiesstore.common.entities.AdminSourceTransferIncidentItem> resolveIncidentPage(
        Long sourceId,
        IncidentStatusFilter statusFilter,
        Pageable pageable
    ) {
        return switch (statusFilter) {
            case ALL -> incidentItemRepository.findByIncidentTransferSourceFromIdOrIncidentTransferSourceToId(sourceId, sourceId, pageable);
            case OPEN -> incidentItemRepository.findOpenBySourceIds(sourceId, sourceId, pageable);
            case REVERTED -> incidentItemRepository.findRevertedBySourceIds(sourceId, sourceId, pageable);
            case CLOSED -> incidentItemRepository.findClosedBySourceIds(sourceId, sourceId, pageable);
        };
    }

    private Map<String, Long> buildIncidentStatusCounts(Long sourceId) {
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        statusCounts.put(IncidentStatusFilter.ALL.name(), incidentItemRepository.countByIncidentTransferSourceFromIdOrIncidentTransferSourceToId(sourceId, sourceId));
        statusCounts.put(IncidentStatusFilter.OPEN.name(), incidentItemRepository.countOpenBySourceIds(sourceId, sourceId));
        statusCounts.put(IncidentStatusFilter.REVERTED.name(), incidentItemRepository.countRevertedBySourceIds(sourceId, sourceId));
        statusCounts.put(IncidentStatusFilter.CLOSED.name(), incidentItemRepository.countClosedBySourceIds(sourceId, sourceId));
        return statusCounts;
    }

    private IncidentStatusFilter normalizeIncidentStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return IncidentStatusFilter.ALL;
        }
        try {
            return IncidentStatusFilter.valueOf(rawStatus.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return IncidentStatusFilter.ALL;
        }
    }

    private String buildIncidentsRedirect(Long sourceId, String status) {
        IncidentStatusFilter normalizedStatus = normalizeIncidentStatus(status);
        return "redirect:/admin/product-sources/" + sourceId + "/manage/incidents?status=" + normalizedStatus.name();
    }

    private String resolveIncidentRedirectTarget(
        Long sourceId,
        String direction,
        String status,
        String incidentStatus,
        String view
    ) {
        if ("incidents".equalsIgnoreCase(view)) {
            return buildIncidentsRedirect(sourceId, incidentStatus);
        }
        return buildTransfersRedirect(sourceId, direction, status);
    }

    private enum IncidentStatusFilter {
        ALL,
        OPEN,
        REVERTED,
        CLOSED
    }
}
