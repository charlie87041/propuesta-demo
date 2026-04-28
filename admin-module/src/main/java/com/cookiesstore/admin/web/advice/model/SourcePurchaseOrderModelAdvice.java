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
import com.cookiesstore.admin.web.controllers.SourcePurchaseOrderController;

@ControllerAdvice(assignableTypes = SourcePurchaseOrderController.class)
public class SourcePurchaseOrderModelAdvice extends BaseAdviceSupport {

    public SourcePurchaseOrderModelAdvice(MessageSource messageSource) {
        super(messageSource);
    }

    @ModelAttribute
    public void populatePurchaseOrderViewModel(
        Model model,
        @PathVariable(value = "sourceId", required = false) Long sourceId,
        @PathVariable(value = "purchaseOrderId", required = false) Long purchaseOrderId,
        @RequestParam(value = "sort", required = false) List<String> sortParams,
        @RequestParam(value = "q", required = false) String searchQuery,
        NativeWebRequest webRequest
    ) {
        model.addAttribute("currentUserId", currentUserId());
        model.addAttribute("sortParams", sortParams);
        model.addAttribute("searchQuery", searchQuery);

        String routeName = resolveRouteName(webRequest);
        if ("admin.product-sources.manage.purchase-orders.list".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.product_sources.manage.purchaseOrders.title"));
            model.addAttribute("activeNav", "product-sources");
            model.addAttribute("manageSection", "purchase-orders");
            model.addAttribute("sourceId", sourceId);
            return;
        }

        if ("admin.product-sources.manage.purchase-orders.new.view".equals(routeName)
            || "admin.product-sources.manage.purchase-orders.new.create".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.product_sources.manage.purchaseOrders.title"));
            model.addAttribute("activeNav", "product-sources");
            model.addAttribute("manageSection", "purchase-orders");
            model.addAttribute("sourceId", sourceId);
            return;
        }

        if ("admin.product-sources.manage.purchase-orders.edit.view".equals(routeName)
            || "admin.product-sources.manage.purchase-orders.edit.update".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.product_sources.manage.purchaseOrders.title"));
            model.addAttribute("activeNav", "product-sources");
            model.addAttribute("manageSection", "purchase-orders");
            model.addAttribute("sourceId", sourceId);
            model.addAttribute("purchaseOrderId", purchaseOrderId);
            return;
        }

        if ("admin.product-sources.manage.purchase-orders.resolve-adhoc.view".equals(routeName)
            || "admin.product-sources.manage.purchase-orders.resolve-adhoc".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.product_sources.manage.purchaseOrders.title"));
            model.addAttribute("activeNav", "product-sources");
            model.addAttribute("manageSection", "purchase-orders");
            model.addAttribute("sourceId", sourceId);
            model.addAttribute("purchaseOrderId", purchaseOrderId);
        }
    }
}
