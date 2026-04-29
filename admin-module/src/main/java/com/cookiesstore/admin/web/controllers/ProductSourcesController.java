package com.cookiesstore.admin.web.controllers;

import com.cookiesstore.admin.config.ProductSourceSearchProperties;
import com.cookiesstore.admin.search.EntitySearchSpecifications;
import com.cookiesstore.admin.service.sources.SourceService;
import com.cookiesstore.admin.web.dto.sources.CreateProductSourceForm;
import com.cookiesstore.common.repositories.SourceRepository;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProductSourcesController {

    private final SourceRepository sourceRepository;
    private final SourceService sourceService;
    private final ProductSourceSearchProperties productSourceSearchProperties;
    private final MessageSource messageSource;

    public ProductSourcesController(
        SourceRepository sourceRepository,
        SourceService sourceService,
        ProductSourceSearchProperties productSourceSearchProperties,
        MessageSource messageSource
    ) {
        this.sourceRepository = sourceRepository;
        this.sourceService = sourceService;
        this.productSourceSearchProperties = productSourceSearchProperties;
        this.messageSource = messageSource;
    }

    @GetMapping(value = "/admin/product-sources", produces = "text/html", name = "admin.product-sources.list")
    public String listProductSources(
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        @ModelAttribute("searchQuery") String searchQuery,
        Model model
    ) {
        var page = StringUtils.hasText(searchQuery)
            ? sourceRepository.findAll(
            EntitySearchSpecifications.globalSearch(
                searchQuery,
                this.productSourceSearchProperties.getSearchableFields()
            ),
            pageable
            )
            : sourceRepository.findAll(pageable);
        model.addAttribute("page", page);
        return "backoffice/product-sources/index";
    }

    @GetMapping(value = "/admin/product-sources/new", produces = "text/html", name = "admin.product-sources.create.view")
    public String newProductSource(Model model) {
        model.addAttribute("form", new CreateProductSourceForm("", "", "", true));
        return "backoffice/product-sources/form";
    }

    @PostMapping(value = "/admin/product-sources", produces = "text/html", name = "admin.product-sources.create")
    public String createProductSource(
        @Valid @ModelAttribute("form") CreateProductSourceForm form,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "backoffice/product-sources/form";
        }

        sourceService.createSource(form);
        redirectAttributes.addFlashAttribute("successMessage", message("admin.product_sources.flash.created"));
        return "redirect:/admin/product-sources";
    }

    @GetMapping(value = "/admin/product-sources/{id}/edit", produces = "text/html", name = "admin.product-sources.edit.view")
    public String editProductSource(@PathVariable("id") Long id, Model model) {
        var source = sourceService.getSource(id);
        model.addAttribute("form", new CreateProductSourceForm(
            source.getCode(),
            source.getName(),
            source.getDescription(),
            source.isActive()
        ));
        return "backoffice/product-sources/form";
    }

    @GetMapping(value = "/admin/product-sources/{id}/manage", produces = "text/html", name = "admin.product-sources.manage.view")
    public String manageProductSource(
        @PathVariable("id") Long id,
        @RequestParam(value = "section", defaultValue = "inventory") String section,
        @RequestParam(value = "poStatus", required = false) String poStatus,
        Model model
    ) {
        var source = sourceService.getSource(id);
        String normalizedSection = normalizeSection(section);
        if ("inventory".equals(normalizedSection)) {
            return "redirect:/admin/product-sources/" + id + "/manage/inventory";
        }
        if ("movements".equals(normalizedSection)) {
            return "redirect:/admin/product-sources/" + id + "/manage/movements";
        }
        if ("transfers".equals(normalizedSection)) {
            return "redirect:/admin/product-sources/" + id + "/manage/transfers";
        }
        if ("incidents".equals(normalizedSection)) {
            return "redirect:/admin/product-sources/" + id + "/manage/incidents";
        }
        if ("purchase-orders".equals(normalizedSection)) {
            String poStatusParam = (poStatus == null || poStatus.isBlank()) ? "ALL" : poStatus.trim().toUpperCase();
            return "redirect:/admin/product-sources/" + id + "/manage/purchase-orders?poStatus=" + poStatusParam;
        }
        if ("pos-users".equals(normalizedSection)) {
            return "redirect:/admin/product-sources/" + id + "/manage/pos-users";
        }
        if ("pos-config".equals(normalizedSection)) {
            return "redirect:/admin/product-sources/" + id + "/manage/pos-config";
        }
        model.addAttribute("source", source);
        model.addAttribute("manageSection", normalizedSection);
        return "backoffice/product-sources/manage";
    }

    @PostMapping(value = "/admin/product-sources/{id}", produces = "text/html", name = "admin.product-sources.update")
    public String updateProductSource(
        @PathVariable("id") Long id,
        @Valid @ModelAttribute("form") CreateProductSourceForm form,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "backoffice/product-sources/form";
        }

        sourceService.updateSource(id, form);
        redirectAttributes.addFlashAttribute("successMessage", message("admin.product_sources.flash.updated"));
        return "redirect:/admin/product-sources";
    }

    @PostMapping(value = "/admin/product-sources/{id}/delete", produces = "text/html", name = "admin.product-sources.delete")
    public String deleteProductSource(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        sourceService.deleteSource(id);
        redirectAttributes.addFlashAttribute("successMessage", message("admin.product_sources.flash.deleted"));
        return "redirect:/admin/product-sources";
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    private String normalizeSection(String section) {
        if (!StringUtils.hasText(section)) {
            return "inventory";
        }
        return switch (section.trim().toLowerCase()) {
            case "inventory", "movements", "purchase-orders", "transfers", "incidents", "alerts", "pos-users", "pos-config" -> section.trim().toLowerCase();
            default -> "inventory";
        };
    }

}
