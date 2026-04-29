package com.cookiesstore.admin.web.advice.model.pos;

import com.cookiesstore.admin.web.advice.support.BaseAdviceSupport;
import com.cookiesstore.admin.web.controllers.pos.SourcePosConfigController;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.context.request.NativeWebRequest;

@ControllerAdvice(assignableTypes = SourcePosConfigController.class)
public class SourcePosConfigModelAdvice extends BaseAdviceSupport {

    public SourcePosConfigModelAdvice(MessageSource messageSource) {
        super(messageSource);
    }

    @ModelAttribute
    public void populate(
        Model model,
        @PathVariable(value = "sourceId", required = false) Long sourceId,
        NativeWebRequest webRequest
    ) {
        model.addAttribute("currentUserId", currentUserId());

        String routeName = resolveRouteName(webRequest);
        if ("admin.product-sources.manage.pos-config.view".equals(routeName)
            || "admin.product-sources.manage.pos-config.update".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.product_sources.manage.pos.config.title"));
            model.addAttribute("activeNav", "product-sources");
            model.addAttribute("manageSection", "pos-config");
            model.addAttribute("sourceId", sourceId);
        }
    }
}
