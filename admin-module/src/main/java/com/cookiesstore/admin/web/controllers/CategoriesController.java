package com.cookiesstore.admin.web.controllers;

import com.cookiesstore.admin.config.CategorySearchProperties;
import com.cookiesstore.admin.search.EntitySearchSpecifications;
import com.cookiesstore.admin.service.categories.CategoryService;
import com.cookiesstore.admin.web.dto.categories.CreateCategoryForm;
import com.cookiesstore.admin.web.dto.categories.UpdateCategoryForm;
import com.cookiesstore.admin.web.dto.products.UpdateProductForm;
import com.cookiesstore.common.entities.Category;
import com.cookiesstore.common.repositories.CategoryRepository;
import com.cookiesstore.common.repositories.ProductRepository;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final CategorySearchProperties categorySearchProperties;
    private final MessageSource messageSource;

    public CategoriesController(
        CategoryRepository categoryRepository,
        ProductRepository productRepository,
        CategoryService categoryService,
        CategorySearchProperties categorySearchProperties,
        MessageSource messageSource
    ) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
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

        Map<Long, CategoryProductCounters> categoryProductCounts = buildCategoryProductCounters(categoriesPage.getContent());

        model.addAttribute("categoriesPage", categoriesPage);
        model.addAttribute("categoryProductCounts", categoryProductCounts);
        return "backoffice/categories/index";
    }

    @GetMapping(value = "/admin/categories/new", produces = "text/html", name = "admin.categories.create.view")
    public String newCategory(Model model) {
        model.addAttribute("form", new CreateCategoryForm("", "", "", "", 0, true, null));
        return "backoffice/categories/form";
    }

    @PostMapping(value = "/admin/categories", name = "admin.categories.create")
    public String createCategory(
        @Valid @ModelAttribute("form") CreateCategoryForm form,
        BindingResult bindingResult,
        Model model,
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
                category.isActive(),
                category.getDefaultTemplate() != null ? category.getDefaultTemplate().getId() : null
            )
        );
       
        return "backoffice/categories/form";
    }

    @PostMapping(value = "/admin/categories/{categoryId}", name = "admin.categories.update")
    public String updateCategory(
        @PathVariable("categoryId") Long categoryId,
        @Valid @ModelAttribute("form") UpdateCategoryForm form,
        BindingResult bindingResult,
        Model model,
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

    private Map<Long, CategoryProductCounters> buildCategoryProductCounters(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return Map.of();
        }

        List<Long> categoryIds = categories.stream()
            .map(Category::getId)
            .toList();

        Map<Long, CategoryProductCounters> countsByCategory = new HashMap<>();
        productRepository.countProductsByCategoryIds(categoryIds).forEach(row -> countsByCategory.put(
            row.getCategoryId(),
            new CategoryProductCounters(
                row.getTotalProducts() == null ? 0L : row.getTotalProducts(),
                row.getActiveProducts() == null ? 0L : row.getActiveProducts(),
                row.getActiveListableProducts() == null ? 0L : row.getActiveListableProducts()
            )
        ));

        // Ensure every category in the current page has a value (including zeros).
        categories.forEach(category -> countsByCategory.putIfAbsent(category.getId(), CategoryProductCounters.ZERO));
        return countsByCategory;
    }

   

    private record CategoryProductCounters(
        long totalProducts,
        long activeProducts,
        long activeListableProducts
    ) {
        private static final CategoryProductCounters ZERO = new CategoryProductCounters(0L, 0L, 0L);
    }
}
