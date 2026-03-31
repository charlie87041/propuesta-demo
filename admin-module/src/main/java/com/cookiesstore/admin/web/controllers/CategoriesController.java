package com.cookiesstore.admin.web.controllers;

import com.cookiesstore.admin.config.CategorySearchProperties;
import com.cookiesstore.admin.search.EntitySearchSpecifications;
import com.cookiesstore.admin.service.categories.CategoryService;
import com.cookiesstore.admin.web.dto.categories.CreateCategoryForm;
import com.cookiesstore.admin.web.dto.categories.UpdateCategoryForm;
import com.cookiesstore.common.repositories.CategoryRepository;
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
public class CategoriesController {

    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final CategorySearchProperties categorySearchProperties;
    private final MessageSource messageSource;

    public CategoriesController(
        CategoryRepository categoryRepository,
        CategoryService categoryService,
        CategorySearchProperties categorySearchProperties,
        MessageSource messageSource
    ) {
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
        this.categorySearchProperties = categorySearchProperties;
        this.messageSource = messageSource;
    }

    @GetMapping(value = "/admin/categories", produces = "text/html", name = "admin.categories.list")
    public String listCategories(
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        @ModelAttribute("searchQuery") String searchQuery,
        Model model
    ) {
        var categoriesPage = StringUtils.hasText(searchQuery)
            ? this.categoryRepository.findAll(
                EntitySearchSpecifications.globalSearch(searchQuery, this.categorySearchProperties.getSearchableFields()),
                pageable
            )
            : this.categoryRepository.findAll(pageable);

        model.addAttribute("categoriesPage", categoriesPage);
        return "backoffice/categories/index";
    }

    @GetMapping(value = "/admin/categories/new", produces = "text/html", name = "admin.categories.create.view")
    public String newCategory(Model model) {
        model.addAttribute("form", new CreateCategoryForm("", "", "", "", 0, true));
        return "backoffice/categories/form";
    }

    @PostMapping(value = "/admin/categories", name = "admin.categories.create")
    public String createCategory(
        @Valid @ModelAttribute("form") CreateCategoryForm form,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "backoffice/categories/form";
        }

        categoryService.createCategory(form);
        redirectAttributes.addFlashAttribute("successMessage", message("admin.categories.flash.created"));
        return "redirect:/admin/categories";
    }

    @GetMapping(value = "/admin/categories/{categoryId}/edit", name = "admin.categories.edit.view")
    public String editCategory(
        @PathVariable("categoryId") Long categoryId,
        Model model
    ) {
        var category = categoryService.getCategory(categoryId);
        model.addAttribute(
            "form",
            new UpdateCategoryForm(
                category.getCode(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getSortOrder(),
                category.isActive()
            )
        );
        return "backoffice/categories/form";
    }

    @PostMapping(value = "/admin/categories/{categoryId}", name = "admin.categories.update")
    public String updateCategory(
        @PathVariable("categoryId") Long categoryId,
        @Valid @ModelAttribute("form") UpdateCategoryForm form,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "backoffice/categories/form";
        }

        categoryService.updateCategory(categoryId, form);
        redirectAttributes.addFlashAttribute("successMessage", message("admin.categories.flash.updated"));
        return "redirect:/admin/categories";
    }

    @PostMapping(value = "/admin/categories/{categoryId}/deactivate", name = "admin.categories.deactivate")
    public String deactivateCategory(
        @PathVariable("categoryId") Long categoryId,
        RedirectAttributes redirectAttributes
    ) {
        categoryService.deactivateCategory(categoryId);
        redirectAttributes.addFlashAttribute("successMessage", message("admin.categories.flash.deactivated"));
        return "redirect:/admin/categories";
    }

    @PostMapping(value = "/admin/categories/{categoryId}/enable", name = "admin.categories.enable")
    public String enableCategory(
        @PathVariable("categoryId") Long categoryId,
        RedirectAttributes redirectAttributes
    ) {
        categoryService.enableCategory(categoryId);
        redirectAttributes.addFlashAttribute("successMessage", message("admin.categories.flash.enabled"));
        return "redirect:/admin/categories";
    }

    @PostMapping(value = "/admin/categories/{categoryId}/delete", name = "admin.categories.delete")
    public String deleteCategory(
        @PathVariable("categoryId") Long categoryId,
        RedirectAttributes redirectAttributes
    ) {
        categoryService.deleteCategory(categoryId);
        redirectAttributes.addFlashAttribute("successMessage", message("admin.categories.flash.deleted"));
        return "redirect:/admin/categories";
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
