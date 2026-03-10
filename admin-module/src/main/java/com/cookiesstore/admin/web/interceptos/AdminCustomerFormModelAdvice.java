package com.cookiesstore.admin.web.interceptos;

import com.cookiesstore.admin.service.AdminUserService;
import com.cookiesstore.admin.web.controllers.AdminCustomerViewController;
import com.cookiesstore.admin.web.controllers.AdminUserViewController;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@ControllerAdvice(assignableTypes = AdminCustomerViewController.class)
public class AdminCustomerFormModelAdvice extends BaseInterceptor{

    private final AdminUserService adminUserService;

    public AdminCustomerFormModelAdvice(AdminUserService adminUserService, MessageSource messageSource) {
        super(messageSource);
        this.adminUserService = adminUserService;
    }

    @ModelAttribute
    public void populateAdminCustomerFormModel(
        Model model,
        @PathVariable(value = "customerId", required = false) Long customerId,
        NativeWebRequest webRequest
    ) {

        Long actorUserId = currentUserId();
        model.addAttribute("currentUserId", actorUserId);
        String routeName = resolveRouteName(webRequest);
        if ("admin.customers.list".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.customers.title"));
            model.addAttribute("activeNav", "customers");
            return;
        }
        if ("admin.customers.create.view".equals(routeName) || "admin.customers.create".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.customers.create"));
            model.addAttribute("activeNav", "customers");
        }
    }

}
