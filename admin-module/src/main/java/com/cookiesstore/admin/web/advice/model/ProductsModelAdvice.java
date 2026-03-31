package com.cookiesstore.admin.web.advice.model;

import com.cookiesstore.admin.web.advice.support.BaseAdviceSupport;
import com.cookiesstore.admin.web.controllers.ProductsController;
import com.cookiesstore.common.repositories.CategoryRepository;
import com.cookiesstore.common.repositories.SourceRepository;
import java.util.List;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;

@ControllerAdvice(assignableTypes = ProductsController.class)
public class ProductsModelAdvice extends BaseAdviceSupport {

    private final CategoryRepository categoryRepository;
    private final SourceRepository sourceRepository;

    public ProductsModelAdvice(
        CategoryRepository categoryRepository,
        SourceRepository sourceRepository,
        MessageSource messageSource
    ) {
        super(messageSource);
        this.categoryRepository = categoryRepository;
        this.sourceRepository = sourceRepository;
    }

    @ModelAttribute
    public void populateProductsViewModel(
        Model model,
        @PathVariable(value = "productId", required = false) Long productId,
        @RequestParam(value = "sort", required = false) List<String> sortParams,
        @RequestParam(value = "q", required = false) String searchQuery,
        NativeWebRequest webRequest
    ) {
        Long actorUserId = currentUserId();
        model.addAttribute("currentUserId", actorUserId);

        String routeName = resolveRouteName(webRequest);
        if ("admin.products.list".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.products.title"));
            model.addAttribute("activeNav", "products");
            model.addAttribute("sortParams", sortParams);
            model.addAttribute("searchQuery", searchQuery);
            return;
        }

        if ("admin.products.create.view".equals(routeName) || "admin.products.create".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.products.create.title"));
            model.addAttribute("activeNav", "products");
            model.addAttribute("isEdit", false);
            model.addAttribute("formAction", "/admin/products");
            model.addAttribute("submitLabel", message("admin.products.submit.create"));
            model.addAttribute("categories", categoryRepository.findAll(Sort.by(Sort.Order.asc("sortOrder"), Sort.Order.asc("name"))));
            model.addAttribute("sources", sourceRepository.findAll(Sort.by(Sort.Order.asc("name"))));
            return;
        }

        if ("admin.products.edit.view".equals(routeName) || "admin.products.update".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.products.edit.title"));
            model.addAttribute("activeNav", "products");
            model.addAttribute("isEdit", true);
            model.addAttribute("productId", productId);
            model.addAttribute("formAction", "/admin/products/" + productId);
            model.addAttribute("submitLabel", message("admin.products.submit.edit"));
            model.addAttribute("categories", categoryRepository.findAll(Sort.by(Sort.Order.asc("sortOrder"), Sort.Order.asc("name"))));
        }
    }
}
