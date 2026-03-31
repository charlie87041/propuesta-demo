package com.cookiesstore.admin.web.advice.model;

import com.cookiesstore.admin.web.advice.support.BaseAdviceSupport;
import com.cookiesstore.admin.web.controllers.CategoriesController;
import java.util.List;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;

@ControllerAdvice(assignableTypes = CategoriesController.class)
public class CategoriesModelAdvice extends BaseAdviceSupport {

    public CategoriesModelAdvice(MessageSource messageSource) {
        super(messageSource);
    }

    @ModelAttribute
    public void populateCategoriesViewModel(
        Model model,
        @PathVariable(value = "categoryId", required = false) Long categoryId,
        @RequestParam(value = "sort", required = false) List<String> sortParams,
        @RequestParam(value = "q", required = false) String searchQuery,
        NativeWebRequest webRequest
    ) {
        Long actorUserId = currentUserId();
        model.addAttribute("currentUserId", actorUserId);

        String routeName = resolveRouteName(webRequest);
        if ("admin.categories.list".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.categories.title"));
            model.addAttribute("activeNav", "categories");
            model.addAttribute("sortParams", sortParams);
            model.addAttribute("searchQuery", searchQuery);
            return;
        }

        if ("admin.categories.create.view".equals(routeName) || "admin.categories.create".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.categories.create.title"));
            model.addAttribute("activeNav", "categories");
            model.addAttribute("isEdit", false);
            model.addAttribute("formAction", "/admin/categories");
            model.addAttribute("submitLabel", message("admin.categories.submit.create"));
            return;
        }

        if ("admin.categories.edit.view".equals(routeName) || "admin.categories.update".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.categories.edit.title"));
            model.addAttribute("activeNav", "categories");
            model.addAttribute("isEdit", true);
            model.addAttribute("categoryId", categoryId);
            model.addAttribute("formAction", "/admin/categories/" + categoryId);
            model.addAttribute("submitLabel", message("admin.categories.submit.edit"));
        }
    }
}
