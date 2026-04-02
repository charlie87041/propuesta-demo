package com.cookiesstore.admin.web.advice.model;

import com.cookiesstore.admin.web.advice.support.BaseAdviceSupport;
import com.cookiesstore.admin.web.controllers.CatalogStockController;
import java.util.List;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.NativeWebRequest;

@ControllerAdvice(assignableTypes = CatalogStockController.class)
public class CatalogStockModelAdvice extends BaseAdviceSupport {

    public CatalogStockModelAdvice(MessageSource messageSource) {
        super(messageSource);
    }

    @ModelAttribute
    public void populateModel(
        Model model,
        @RequestParam(value = "sort", required = false) List<String> sortParams,
        @RequestParam(value = "q", required = false) String searchQuery,
        NativeWebRequest webRequest
    ) {
        model.addAttribute("currentUserId", currentUserId());

        String routeName = resolveRouteName(webRequest);
        if ("admin.catalog.stock.list".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.catalog.stock.title"));
            model.addAttribute("activeNav", "stock");
            model.addAttribute("sortParams", sortParams);
            model.addAttribute("searchQuery", searchQuery);
        }
    }
}
