package com.cookiesstore.admin.web.advice.model.pos;

import com.cookiesstore.admin.web.advice.support.BaseAdviceSupport;
import com.cookiesstore.admin.web.controllers.pos.SourcePosUserController;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.context.request.NativeWebRequest;

@ControllerAdvice(assignableTypes = SourcePosUserController.class)
public class SourcePosUserModelAdvice extends BaseAdviceSupport {

    public SourcePosUserModelAdvice(MessageSource messageSource) {
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
        if (
            "admin.product-sources.manage.pos-users.list".equals(routeName)
                || "admin.product-sources.manage.pos-users.create".equals(routeName)
                || "admin.product-sources.manage.pos-users.delete".equals(routeName)
        ) {
            model.addAttribute("pageTitle", message("admin.product_sources.manage.pos.users.title"));
            model.addAttribute("activeNav", "product-sources");
            model.addAttribute("manageSection", "pos-users");
            model.addAttribute("sourceId", sourceId);
        }
    }
}
