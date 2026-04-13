package com.cookiesstore.admin.web.advice.model;

import com.cookiesstore.admin.web.advice.support.BaseAdviceSupport;
import com.cookiesstore.admin.web.controllers.CurrenciesController;
import java.util.List;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;

@ControllerAdvice(assignableTypes = CurrenciesController.class)
public class CurrenciesModelAdvice extends BaseAdviceSupport {

    public CurrenciesModelAdvice(MessageSource messageSource) {
        super(messageSource);
    }

    @ModelAttribute
    public void populateCurrenciesViewModel(
        Model model,
        @RequestParam(value = "sort", required = false) List<String> sortParams,
        @RequestParam(value = "q", required = false) String searchQuery,
        NativeWebRequest webRequest
    ) {
        Long actorUserId = currentUserId();
        model.addAttribute("currentUserId", actorUserId);

        String routeName = resolveRouteName(webRequest);
        if ("settings.currencies.list".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.settings.currencies.title"));
            model.addAttribute("activeNav", "settings-currencies");
            model.addAttribute("sortParams", sortParams);
            model.addAttribute("searchQuery", searchQuery);
            return;
        }

        if ("settings.currencies.create".equals(routeName) || "settings.currencies.create,view".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.settings.currencies.create.title"));
            model.addAttribute("activeNav", "settings-currencies");
            model.addAttribute("isEdit", false);
            model.addAttribute("formAction", "/admin/settings/currencies/create");
            model.addAttribute("submitLabel", message("admin.settings.currencies.submit.create"));
            return;
        }

        if ("settings.currencies.edit.view".equals(routeName) || "settings.currencies.update".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.settings.currencies.edit.title"));
            model.addAttribute("activeNav", "settings-currencies");
            model.addAttribute("isEdit", true);
            model.addAttribute("formAction", resolveEditFormAction(webRequest));
            model.addAttribute("submitLabel", message("admin.settings.currencies.submit.edit"));
        }
    }

    private String resolveEditFormAction(NativeWebRequest webRequest) {
        jakarta.servlet.http.HttpServletRequest request = webRequest.getNativeRequest(jakarta.servlet.http.HttpServletRequest.class);
        if (request == null) {
            return "/admin/settings/currencies";
        }
        String uri = request.getRequestURI();
        if (uri == null) {
            return "/admin/settings/currencies";
        }
        if (uri.endsWith("/edit")) {
            return uri.substring(0, uri.length() - "/edit".length());
        }
        return uri;
    }
}
