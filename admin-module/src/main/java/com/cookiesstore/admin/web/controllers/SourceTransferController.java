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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;

import com.cookiesstore.admin.service.sources.SourceService;
import com.cookiesstore.admin.service.sources.SourceTransferService;
import com.cookiesstore.admin.web.dto.sources.CreateSourceTransfer;
import com.cookiesstore.admin.web.dto.sources.TransferIncidentActionForm;
import com.cookiesstore.common.entities.ProductSourceStatus;
import com.cookiesstore.common.entities.Source;
import com.cookiesstore.common.repositories.AdminSourceTransferIncidentItemRepository;
import com.cookiesstore.common.repositories.AdminSourceTransferItemRepository;
import com.cookiesstore.common.repositories.AdminSourceTransferRepository;
import com.cookiesstore.common.repositories.ProductSourceRepository;
import com.cookiesstore.common.repositories.SourceRepository;

import jakarta.validation.Valid;

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
        @RequestParam(value = "direction", defaultValue = "inbound") String direction
    ){
        String normalizedDirection = "outbound".equalsIgnoreCase(direction) ? "outbound" : "inbound";
        var transferPage = "inbound".equals(normalizedDirection)
            ? transferRepository.findBySourceToId(sourceId, page)
            : transferRepository.findBySourceFromId(sourceId, page);
        var inboundPreview = transferRepository.findFirstBySourceToIdOrderByUpdatedAtDesc(sourceId).orElse(null);
        var outboundPreview = transferRepository.findFirstBySourceFromIdOrderByUpdatedAtDesc(sourceId).orElse(null);
        long inboundCount = transferRepository.countBySourceToId(sourceId);
        long outboundCount = transferRepository.countBySourceFromId(sourceId);
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
        model.addAttribute("manageSection", "transfers");
        return "backoffice/product-sources/manage/transfer_list";
    }

    @GetMapping(value = "/admin/product-sources/{sourceId}/manage/incidents", name = "admin.product-sources.manage.incidents.list")
    public String listTransferIncidents(
        Model model,
        @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable page,
        @PathVariable("sourceId") Long sourceId
    ) {
        var incidentPage = incidentItemRepository
            .findByIncidentTransferSourceFromIdOrIncidentTransferSourceToId(sourceId, sourceId, page);
        model.addAttribute("source", sourceService.getSource(sourceId));
        model.addAttribute("incidentPage", incidentPage);
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
        @RequestParam(value = "direction", defaultValue = "outbound") String direction
    ) {
        this.sourceTransferService.markTransferInTransit(sourceId, transferId, currentUserId);
        return "redirect:/admin/product-sources/" + sourceId + "/manage/transfers?direction=" + direction;
    }

    @PostMapping(value = "/admin/product-sources/{sourceId}/manage/transfers/{transferId}/delete", produces = "text/html")
    public String deleteTransfer(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("transferId") Long transferId,
        @RequestParam(value = "direction", defaultValue = "outbound") String direction
    ) {
        this.sourceTransferService.deleteDraftTransfer(sourceId, transferId);
        return "redirect:/admin/product-sources/" + sourceId + "/manage/transfers?direction=" + direction;
    }

    @PostMapping(value = "/admin/product-sources/{sourceId}/manage/transfers/{transferId}/cancel", produces = "text/html")
    public String cancelTransfer(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("transferId") Long transferId,
        @ModelAttribute("currentUserId") Long currentUserId,
        @RequestParam(value = "direction", defaultValue = "outbound") String direction
    ) {
        this.sourceTransferService.cancelTransfer(sourceId, transferId, currentUserId);
        return "redirect:/admin/product-sources/" + sourceId + "/manage/transfers?direction=" + direction;
    }

    @PostMapping(value = "/admin/product-sources/{sourceId}/manage/transfers/{transferId}/complete", produces = "text/html")
    public String completeTransfer(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("transferId") Long transferId,
        @ModelAttribute("currentUserId") Long currentUserId,
        @RequestParam(value = "direction", defaultValue = "outbound") String direction
    ) {
        this.sourceTransferService.completeTransfer(sourceId, transferId, currentUserId);
        return "redirect:/admin/product-sources/" + sourceId + "/manage/transfers?direction=" + direction;
    }

    @PostMapping(value = "/admin/product-sources/{sourceId}/manage/transfers/{transferId}/cancel-with-incident", produces = "text/html")
    public String cancelTransferWithIncident(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("transferId") Long transferId,
        @ModelAttribute("currentUserId") Long currentUserId,
        @RequestParam(value = "direction", defaultValue = "outbound") String direction,
        @Valid @ModelAttribute("incidentForm") TransferIncidentActionForm incidentForm
    ) {
        this.sourceTransferService.cancelTransferWithIncident(sourceId, transferId, currentUserId, incidentForm);
        return "redirect:/admin/product-sources/" + sourceId + "/manage/transfers?direction=" + direction;
    }

    @PostMapping(value = "/admin/product-sources/{sourceId}/manage/transfers/{transferId}/complete-with-incident", produces = "text/html")
    public String completeTransferWithIncident(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("transferId") Long transferId,
        @ModelAttribute("currentUserId") Long currentUserId,
        @RequestParam(value = "direction", defaultValue = "outbound") String direction,
        @Valid @ModelAttribute("incidentForm") TransferIncidentActionForm incidentForm
    ) {
        this.sourceTransferService.completeTransferWithIncident(sourceId, transferId, currentUserId, incidentForm);
        return "redirect:/admin/product-sources/" + sourceId + "/manage/transfers?direction=" + direction;
    }

    @PostMapping(value = "/admin/product-sources/{sourceId}/manage/transfers/incidents/{incidentItemId}/close", produces = "text/html")
    public String closeIncident(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("incidentItemId") Long incidentItemId,
        @ModelAttribute("currentUserId") Long currentUserId,
        @RequestParam(value = "direction", defaultValue = "outbound") String direction,
        @RequestParam(value = "view", required = false) String view
    ) {
        this.sourceTransferService.closeIncident(sourceId, incidentItemId, currentUserId);
        return resolveIncidentRedirectTarget(sourceId, direction, view);
    }

    @PostMapping(value = "/admin/product-sources/{sourceId}/manage/transfers/incidents/{incidentItemId}/revert", produces = "text/html")
    public String revertIncident(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("incidentItemId") Long incidentItemId,
        @ModelAttribute("currentUserId") Long currentUserId,
        @RequestParam(value = "direction", defaultValue = "outbound") String direction,
        @RequestParam(value = "view", required = false) String view
    ) {
        this.sourceTransferService.revertIncident(sourceId, incidentItemId, currentUserId);
        return resolveIncidentRedirectTarget(sourceId, direction, view);
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

    private String resolveIncidentRedirectTarget(Long sourceId, String direction, String view) {
        if ("incidents".equalsIgnoreCase(view)) {
            return "redirect:/admin/product-sources/" + sourceId + "/manage/incidents";
        }
        return "redirect:/admin/product-sources/" + sourceId + "/manage/transfers?direction=" + direction;
    }
}
