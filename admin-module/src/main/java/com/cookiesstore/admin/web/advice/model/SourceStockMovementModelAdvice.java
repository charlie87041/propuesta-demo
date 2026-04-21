package com.cookiesstore.admin.web.advice.model;

import com.cookiesstore.admin.web.advice.support.BaseAdviceSupport;
import com.cookiesstore.admin.web.controllers.SourceStockMovementController;
import java.util.List;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;

@ControllerAdvice(assignableTypes = SourceStockMovementController.class)
public class SourceStockMovementModelAdvice extends BaseAdviceSupport {

    public SourceStockMovementModelAdvice(MessageSource messageSource) {
        super(messageSource);
    }

    @ModelAttribute
    public void populateStockMovementViewModel(
        Model model,
        @PathVariable(value = "sourceId", required = false) Long sourceId,
        @RequestParam(value = "sort", required = false) List<String> sortParams,
        @RequestParam(value = "q", required = false) String searchQuery,
        NativeWebRequest webRequest
    ) {
        model.addAttribute("currentUserId", currentUserId());

        String routeName = resolveRouteName(webRequest);
        if ("admin.sources.manage.list".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.product_sources.manage.title"));
            model.addAttribute("activeNav", "product-sources");
            model.addAttribute("manageSection", "movements");
            model.addAttribute("sourceId", sourceId);
            model.addAttribute("sortParams", sortParams);
            model.addAttribute("searchQuery", searchQuery);
        }
    }
}
