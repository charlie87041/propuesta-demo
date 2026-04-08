package com.cookiesstore.admin.web.advice.model;

import com.cookiesstore.admin.web.advice.support.BaseAdviceSupport;
import com.cookiesstore.admin.web.controllers.ProductTemplateController;
import java.util.List;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;

@ControllerAdvice(assignableTypes = ProductTemplateController.class)
public class ProductTemplateModelAdvice extends BaseAdviceSupport {

    public ProductTemplateModelAdvice(MessageSource messageSource) {
        super(messageSource);
    }

    @ModelAttribute
    public void populateProductTemplateViewModel(
        Model model,
        @PathVariable(value = "productTemplateId", required = false) Long productTemplateId,
        @RequestParam(value = "sort", required = false) List<String> sortParams,
        @RequestParam(value = "q", required = false) String searchQuery,
        NativeWebRequest webRequest
    ) {
        Long actorUserId = currentUserId();
        model.addAttribute("currentUserId", actorUserId);

        String routeName = resolveRouteName(webRequest);
        String uri = webRequest.getNativeRequest(jakarta.servlet.http.HttpServletRequest.class) != null
            ? webRequest.getNativeRequest(jakarta.servlet.http.HttpServletRequest.class).getRequestURI()
            : "";

        if (isListRoute(routeName, uri)) {
            model.addAttribute("pageTitle", message("admin.product_templates.title"));
            model.addAttribute("activeNav", "product-templates");
            model.addAttribute("sortParams", sortParams);
            model.addAttribute("searchQuery", searchQuery);
            return;
        }

        if (isCreateRoute(routeName, uri)) {
            model.addAttribute("pageTitle", message("admin.product_templates.create.title"));
            model.addAttribute("activeNav", "product-templates");
            model.addAttribute("isEdit", false);
            model.addAttribute("formAction", "/admin/product-template");
            model.addAttribute("submitLabel", message("admin.product_templates.submit.create"));
            return;
        }

        if (isDetailRoute(routeName, uri)) {
            model.addAttribute("pageTitle", message("admin.product_templates.edit.title"));
            model.addAttribute("activeNav", "product-templates");
            model.addAttribute("isEdit", true);
            model.addAttribute("productTemplateId", productTemplateId);
            model.addAttribute("formAction", "/admin/product-template/" + productTemplateId);
            model.addAttribute("submitLabel", message("admin.product_templates.submit.edit"));
        }
    }

    private boolean isListRoute(String routeName, String uri) {
        if ("admin.product-templates.list".equals(routeName)) {
            return true;
        }
        return routeName == null && "/admin/product-template".equals(uri);
    }

    private boolean isCreateRoute(String routeName, String uri) {
        return "admin.product-templates.create".equals(routeName)
            || "admin.product-templates.create.view".equals(routeName);
    }

    private boolean isDetailRoute(String routeName, String uri) {
        if ("admin.product-templates.edit.view".equals(routeName) || "admin.product-templates.update".equals(routeName)) {
            return true;
        }
        return uri != null && uri.matches("^/admin/product-template/\\d+$");
    }
}
