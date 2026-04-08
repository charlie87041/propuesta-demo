package com.cookiesstore.admin.web.controllers;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cookiesstore.admin.config.ProductTemplateSearchProperties;
import com.cookiesstore.admin.search.EntitySearchSpecifications;
import com.cookiesstore.admin.service.products.ProductTemplateService;
import com.cookiesstore.admin.web.dto.products.ProductTemplateFieldForm;
import com.cookiesstore.admin.web.dto.products.UpdateProductTemplateForm;

import jakarta.validation.Valid;
import java.util.List;

@Controller
public class ProductTemplateController {

    private ProductTemplateService productTemplateService;
    private ProductTemplateSearchProperties productTemplateSearchProperties;
    private final MessageSource messageSource;

    public ProductTemplateController(ProductTemplateService productTemplateService, ProductTemplateSearchProperties productTemplateSearchProperties, MessageSource messageSource) {
        this.productTemplateService = productTemplateService;
        this.productTemplateSearchProperties = productTemplateSearchProperties;
        this.messageSource = messageSource;
    }

    @GetMapping(value = "admin/product-template", name = "admin.product-templates.list")
    public String listProductTemplates(
        Model model,
        @PageableDefault(size = 10) Pageable pageable,
        @ModelAttribute("searchQuery") String searchQuery
    ) {
        var templatePage = !StringUtils.hasText(searchQuery)
            ? productTemplateService.pageAllProductTemplates(pageable) 
            : productTemplateService.pageAllProductTemplates(
                pageable, 
                EntitySearchSpecifications.globalSearch(searchQuery, productTemplateSearchProperties.getSearchableFields())
            );
        model.addAttribute("productTemplates", templatePage);
        return "backoffice/product_template/list";
    }

    @GetMapping(value = "admin/product-template/new", name = "admin.product-templates.create.view")
    public String newProductTemplate(Model model) {
        if (!(model.asMap().get("form") instanceof UpdateProductTemplateForm)) {
            model.addAttribute(
                "form",
                new UpdateProductTemplateForm("", "", "", true, List.of())
            );
        }
        return "backoffice/product_template/form";
    }

    @PostMapping(value = "admin/product-template", name = "admin.product-templates.create")
    public String createProductTemplates(
        @Valid @ModelAttribute("form") UpdateProductTemplateForm form,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "backoffice/product_template/form";
        }
        productTemplateService.createProductTemplate(form);
        redirectAttributes.addFlashAttribute("successMessage", message("admin.product_templates.flash.created"));
        return "redirect:/admin/product-template";
    }

    @GetMapping(value = "admin/product-template/{productTemplateId}", name = "admin.product-templates.edit.view")
    public String getProductTemplate(
        Model model,
        @PathVariable("productTemplateId") Long productTemplateId
    ) {
        var productTemplate = productTemplateService.findById(productTemplateId);
        var form = new UpdateProductTemplateForm(
            productTemplate.getCode(),
            productTemplate.getName(),
            productTemplate.getDescription(),
            productTemplate.isActive(),
            productTemplate.getFields().stream()
                .sorted(java.util.Comparator.comparingInt(com.cookiesstore.common.entities.ProductTemplateField::getSortOrder))
                .map(field -> new ProductTemplateFieldForm(
                productTemplate.getId(),
                field.getFieldKey(),
                field.getLabel(),
                field.getFieldType(),
                field.isRequired(),
                field.getDefaultValue(),
                field.getValidationRules(),
                (byte) field.getSortOrder(),
                field.getId()
            )).toList()
        );
        model.addAttribute("form", form);
        return "backoffice/product_template/form";
    }

     @PostMapping(value = "admin/product-template/{productTemplateId}", name = "admin.product-templates.update")
    public String updateProductTemplate(
        @PathVariable("productTemplateId") Long productTemplateId,
        @Valid @ModelAttribute("form") UpdateProductTemplateForm form,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "backoffice/product_template/form";
        }
        productTemplateService.updateProductTemplate(form, productTemplateId);
        redirectAttributes.addFlashAttribute("successMessage", message("admin.product_templates.flash.updated"));
        return "redirect:/admin/product-template";
    }


     @PostMapping(value = "admin/product-template/{productTemplateId}/delete", name = "admin.product-templates.delete")
    public String deleteProductTemplate(
        @PathVariable("productTemplateId") Long productTemplateId,
        RedirectAttributes redirectAttributes
    ) {
        productTemplateService.deleteProductTemplate(productTemplateId);
        redirectAttributes.addFlashAttribute("successMessage", message("admin.product_templates.flash.deleted"));
        return "redirect:/admin/product-template";
    }


    @PostMapping(value = "admin/product-template/{productTemplateId}/deactivate", name = "admin.product-templates.deactivate")
    public String deactivateProductTemplate(
        @PathVariable("productTemplateId") Long productTemplateId,
        RedirectAttributes redirectAttributes
    ) {
        productTemplateService.deactivateProductTemplate(productTemplateId);
        redirectAttributes.addFlashAttribute("successMessage", message("admin.product_templates.flash.deactivated"));
        return "redirect:/admin/product-template";
    }

    @PostMapping(value = "admin/product-template/{productTemplateId}/enable", name = "admin.product-templates.enable")
    public String enableProductTemplate(
        @PathVariable("productTemplateId") Long productTemplateId,
        RedirectAttributes redirectAttributes
    ) {
        productTemplateService.enableProductTemplate(productTemplateId);
        redirectAttributes.addFlashAttribute("successMessage", message("admin.product_templates.flash.enabled"));
        return "redirect:/admin/product-template";
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
