package com.cookiesstore.admin.web.controllers;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cookiesstore.admin.service.sources.SourceInventoryProductService;
import com.cookiesstore.admin.service.sources.SourceService;
import com.cookiesstore.admin.web.dto.sources.UpdateSourceProductForm;

import jakarta.validation.Valid;

@Controller
public class SourceInventoryProductController {

    private final SourceInventoryProductService sourceInventoryProductService;
    private final SourceService sourceService;
    private final MessageSource messageSource;

    public SourceInventoryProductController(
        SourceInventoryProductService sourceInventoryProductService,
        SourceService sourceService,
        MessageSource messageSource
    ) {
        this.sourceInventoryProductService = sourceInventoryProductService;
        this.sourceService = sourceService;
        this.messageSource = messageSource;
    }

    @GetMapping(
        value = "/admin/product-sources/{sourceId}/manage/inventory",
        produces = "text/html",
        name = "admin.product-sources.manage.inventory.list"
    )
    public String list(
        @PathVariable("sourceId") Long sourceId,
        @RequestParam(value = "q", required = false) String searchQuery,
        @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable,
        Model model
    ) {
        model.addAttribute("source", sourceService.getSource(sourceId));
        model.addAttribute("manageSection", "inventory");
        model.addAttribute("stockPage", sourceInventoryProductService.listBySource(sourceId, searchQuery, pageable));
        return "backoffice/product-sources/manage/inventory_list";
    }

    @GetMapping(
        value = "/admin/product-sources/{sourceId}/manage/inventory/{productId}",
        produces = "text/html",
        name = "admin.product-sources.manage.inventory.show"
    )
    public String show(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("productId") Long productId,
        Model model
    ) {
        var row = sourceInventoryProductService.findBySourceAndProduct(sourceId, productId);
        model.addAttribute("source", sourceService.getSource(sourceId));
        model.addAttribute("manageSection", "inventory");
        model.addAttribute("row", row);
        return "backoffice/product-sources/manage/inventory_show";
    }

    @GetMapping(
        value = "/admin/product-sources/{sourceId}/manage/inventory/{productId}/edit",
        produces = "text/html",
        name = "admin.product-sources.manage.inventory.edit.view"
    )
    public String edit(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("productId") Long productId,
        Model model
    ) {
        var row = sourceInventoryProductService.findBySourceAndProduct(sourceId, productId);
        model.addAttribute("source", sourceService.getSource(sourceId));
        model.addAttribute("manageSection", "inventory");
        model.addAttribute("row", row);
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", sourceInventoryProductService.buildForm(sourceId, productId));
        }
        model.addAttribute("formAction", "/admin/product-sources/" + sourceId + "/manage/inventory/" + productId);
        return "backoffice/product-sources/manage/inventory_form";
    }

    @PostMapping(
        value = "/admin/product-sources/{sourceId}/manage/inventory/{productId}",
        produces = "text/html",
        name = "admin.product-sources.manage.inventory.update"
    )
    public String update(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("productId") Long productId,
        @Valid @ModelAttribute("form") UpdateSourceProductForm form,
        BindingResult bindingResult,
        @ModelAttribute("currentUserId") Long currentUserId,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        var row = sourceInventoryProductService.findBySourceAndProduct(sourceId, productId);
        if (bindingResult.hasErrors()) {
            model.addAttribute("source", sourceService.getSource(sourceId));
            model.addAttribute("manageSection", "inventory");
            model.addAttribute("row", row);
            model.addAttribute("formAction", "/admin/product-sources/" + sourceId + "/manage/inventory/" + productId);
            return "backoffice/product-sources/manage/inventory_form";
        }

        sourceInventoryProductService.update(sourceId, productId, form, currentUserId);
        redirectAttributes.addFlashAttribute(
            "successMessage",
            message("admin.product_sources.manage.inventory.flash.updated")
        );
        return "redirect:/admin/product-sources/" + sourceId + "/manage/inventory/" + productId;
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
