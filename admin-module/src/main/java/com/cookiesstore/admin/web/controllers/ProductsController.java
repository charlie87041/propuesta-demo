package com.cookiesstore.admin.web.controllers;

import com.cookiesstore.admin.config.ProductSearchProperties;
import com.cookiesstore.admin.search.EntitySearchSpecifications;
import com.cookiesstore.admin.service.products.ProductService;
import com.cookiesstore.admin.web.dto.products.CreateProductForm;
import com.cookiesstore.admin.web.dto.products.UpdateProductForm;
import com.cookiesstore.common.repositories.ProductRepository;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProductsController {

    private final ProductRepository productRepository;
    private final ProductService productService;
    private final ProductSearchProperties productSearchProperties;
    private final MessageSource messageSource;

    public ProductsController(
        ProductRepository productRepository,
        ProductService productService,
        ProductSearchProperties productSearchProperties,
        MessageSource messageSource
    ) {
        this.productRepository = productRepository;
        this.productService = productService;
        this.productSearchProperties = productSearchProperties;
        this.messageSource = messageSource;
    }

    @GetMapping(value = "/admin/products", produces = "text/html", name = "admin.products.list")
    public String listProducts(
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        @ModelAttribute("searchQuery") String searchQuery,
        Model model
    ) {
        var productsPage = StringUtils.hasText(searchQuery)
            ? productRepository.findAll(
                EntitySearchSpecifications.globalSearch(searchQuery, productSearchProperties.getSearchableFields()),
                pageable
            )
            : productRepository.findAll(pageable);

        model.addAttribute("productsPage", productsPage);
        return "backoffice/products/index";
    }

    @GetMapping(value = "/admin/products/new", produces = "text/html", name = "admin.products.create.view")
    public String newProduct(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new CreateProductForm("", "", "", "", null, "", "", "", "", true, true, null, 0, 20, null, null, null, null));
        }
        return "backoffice/products/form";
    }

    @PostMapping(value = "/admin/products", name = "admin.products.create")
    public String createProduct(
        @Valid @ModelAttribute("form") CreateProductForm form,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "backoffice/products/form";
        }

        productService.createProduct(form);
        redirectAttributes.addFlashAttribute("successMessage", message("admin.products.flash.created"));
        return "redirect:/admin/products";
    }

    @GetMapping(value = "/admin/products/{productId}/edit", name = "admin.products.edit.view")
    public String editProduct(
        @PathVariable("productId") Long productId,
        Model model
    ) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", productService.buildUpdateProductForm(productId));
        }
        return "backoffice/products/form";
    }

    @PostMapping(value = "/admin/products/{productId}", name = "admin.products.update")
    public String updateProduct(
        @PathVariable("productId") Long productId,
        @Valid @ModelAttribute("form") UpdateProductForm form,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "backoffice/products/form";
        }

        productService.updateProduct(productId, form);
        redirectAttributes.addFlashAttribute("successMessage", message("admin.products.flash.updated"));
        return "redirect:/admin/products";
    }

    @PostMapping(value = "/admin/products/{productId}/deactivate", name = "admin.products.deactivate")
    public String deactivateProduct(
        @PathVariable("productId") Long productId,
        RedirectAttributes redirectAttributes
    ) {
        productService.deactivateProduct(productId);
        redirectAttributes.addFlashAttribute("successMessage", message("admin.products.flash.deactivated"));
        return "redirect:/admin/products";
    }

    @PostMapping(value = "/admin/products/{productId}/enable", name = "admin.products.enable")
    public String enableProduct(
        @PathVariable("productId") Long productId,
        RedirectAttributes redirectAttributes
    ) {
        productService.enableProduct(productId);
        redirectAttributes.addFlashAttribute("successMessage", message("admin.products.flash.enabled"));
        return "redirect:/admin/products";
    }

    @PostMapping(value = "/admin/products/{productId}/delete", name = "admin.products.delete")
    public String deleteProduct(
        @PathVariable("productId") Long productId,
        RedirectAttributes redirectAttributes
    ) {
        productService.deleteProduct(productId);
        redirectAttributes.addFlashAttribute("successMessage", message("admin.products.flash.deleted"));
        return "redirect:/admin/products";
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
