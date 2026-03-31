package com.cookiesstore.admin.web.advice.model;

import com.cookiesstore.admin.web.advice.support.BaseAdviceSupport;
import com.cookiesstore.admin.web.controllers.ProductSourcesController;
import java.util.List;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;

@ControllerAdvice(assignableTypes = ProductSourcesController.class)
public class ProductSourcesModelAdvice extends BaseAdviceSupport {

    public ProductSourcesModelAdvice(MessageSource messageSource) {
        super(messageSource);
    }

    @ModelAttribute
    public void populateProductSourcesViewModel(
        Model model,
        @PathVariable(value = "id", required = false) Long sourceId,
        @RequestParam(value = "sort", required = false) List<String> sortParams,
        @RequestParam(value = "q", required = false) String searchQuery,
        NativeWebRequest webRequest
    ) {
        Long actorUserId = currentUserId();
        model.addAttribute("currentUserId", actorUserId);

        String routeName = resolveRouteName(webRequest);
        if ("admin.product-sources.list".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.product_sources.title"));
            model.addAttribute("activeNav", "product-sources");
            model.addAttribute("sortParams", sortParams);
            model.addAttribute("searchQuery", searchQuery);
            return;
        }

        if ("admin.product-sources.create.view".equals(routeName) || "admin.product-sources.create".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.product_sources.create.title"));
            model.addAttribute("activeNav", "product-sources");
            model.addAttribute("isEdit", false);
            model.addAttribute("formAction", "/admin/product-sources");
            model.addAttribute("submitLabel", message("admin.product_sources.submit.create"));
            return;
        }

        if ("admin.product-sources.edit.view".equals(routeName) || "admin.product-sources.update".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.product_sources.edit.title"));
            model.addAttribute("activeNav", "product-sources");
            model.addAttribute("isEdit", true);
            model.addAttribute("sourceId", sourceId);
            model.addAttribute("formAction", "/admin/product-sources/" + sourceId);
            model.addAttribute("submitLabel", message("admin.product_sources.submit.edit"));
        }
    }
}
