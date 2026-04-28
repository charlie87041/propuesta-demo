package com.cookiesstore.admin.web.advice.model;

import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;

import com.cookiesstore.admin.web.advice.support.BaseAdviceSupport;
import com.cookiesstore.admin.web.controllers.SourceInventoryProductController;

@ControllerAdvice(assignableTypes = SourceInventoryProductController.class)
public class SourceInventoryProductModelAdvice extends BaseAdviceSupport {

    public SourceInventoryProductModelAdvice(MessageSource messageSource) {
        super(messageSource);
    }

    @ModelAttribute
    public void populate(
        Model model,
        @PathVariable(value = "sourceId", required = false) Long sourceId,
        @RequestParam(value = "q", required = false) String searchQuery,
        @RequestParam(value = "sort", required = false) List<String> sortParams,
        NativeWebRequest webRequest
    ) {
        model.addAttribute("currentUserId", currentUserId());

        String routeName = resolveRouteName(webRequest);
        if ("admin.product-sources.manage.inventory.list".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.product_sources.manage.inventory.title"));
            model.addAttribute("activeNav", "product-sources");
            model.addAttribute("manageSection", "inventory");
            model.addAttribute("sourceId", sourceId);
            model.addAttribute("searchQuery", searchQuery);
            model.addAttribute("sortParams", sortParams);
            return;
        }

        if ("admin.product-sources.manage.inventory.show".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.product_sources.manage.inventory.show.title"));
            model.addAttribute("activeNav", "product-sources");
            model.addAttribute("manageSection", "inventory");
            model.addAttribute("sourceId", sourceId);
            return;
        }

        if ("admin.product-sources.manage.inventory.edit.view".equals(routeName)
            || "admin.product-sources.manage.inventory.update".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.product_sources.manage.inventory.edit.title"));
            model.addAttribute("activeNav", "product-sources");
            model.addAttribute("manageSection", "inventory");
            model.addAttribute("sourceId", sourceId);
        }
    }
}
