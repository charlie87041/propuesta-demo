package com.cookiesstore.admin.web.advice.model;

import com.cookiesstore.admin.web.advice.support.BaseAdviceSupport;
import com.cookiesstore.admin.web.controllers.SourceTransferController;
import java.util.List;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;

@ControllerAdvice(assignableTypes = SourceTransferController.class)
public class SourceTransferModelAdvice extends BaseAdviceSupport {

    public SourceTransferModelAdvice(MessageSource messageSource) {
        super(messageSource);
    }

    @ModelAttribute
    public void populateTransferViewModel(
        Model model,
        @PathVariable(value = "sourceId", required = false) Long sourceId,
        @RequestParam(value = "sort", required = false) List<String> sortParams,
        @RequestParam(value = "q", required = false) String searchQuery,
        @RequestParam(value = "direction", required = false) String direction,
        NativeWebRequest webRequest
    ) {
        model.addAttribute("currentUserId", currentUserId());
        model.addAttribute("sortParams", sortParams);
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("direction", direction == null ? "inbound" : direction);

        String routeName = resolveRouteName(webRequest);
        if ("admin.product-sources.manage.transfers.list".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.product_sources.manage.title"));
            model.addAttribute("activeNav", "product-sources");
            model.addAttribute("manageSection", "transfers");
            model.addAttribute("sourceId", sourceId);
            return;
        }

        if ("admin.product-sources.manage.incidents.list".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.product_sources.manage.title"));
            model.addAttribute("activeNav", "product-sources");
            model.addAttribute("manageSection", "incidents");
            model.addAttribute("sourceId", sourceId);
            return;
        }

        if ("admin.product-sources.manage.transfers.new.view".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.product_sources.manage.transfers.new.title"));
            model.addAttribute("activeNav", "product-sources");
            model.addAttribute("sourceId", sourceId);
        }
    }
}
