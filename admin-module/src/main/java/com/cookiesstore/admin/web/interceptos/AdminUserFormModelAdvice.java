package com.cookiesstore.admin.web.interceptos;

import com.cookiesstore.admin.service.AdminUserService;
import com.cookiesstore.admin.web.controllers.AdminUserViewController;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@ControllerAdvice(assignableTypes = AdminUserViewController.class)
public class AdminUserFormModelAdvice extends BaseInterceptor{

    private final AdminUserService adminUserService;

    public AdminUserFormModelAdvice(AdminUserService adminUserService, MessageSource messageSource) {
        super(messageSource);
        this.adminUserService = adminUserService;
      
    }

    @ModelAttribute
    public void populateAdminUserFormModel(
        Model model,
        @PathVariable(value = "userId", required = false) Long userId,
        @RequestParam(value = "roleCode", required = false) String roleCode,
        NativeWebRequest webRequest
    ) {
        if (model.containsAttribute("roles")) {
            return;
        }

        Long actorUserId = currentUserId();
        model.addAttribute("currentUserId", actorUserId);
        String routeName = resolveRouteName(webRequest);
        if ("admin.users.list".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.users.title"));
            model.addAttribute("activeNav", "users");
            return;
        }
        String domainCode;
        if (userId != null) {
            try {
                domainCode = adminUserService.resolveUserDomainCode(userId);
            } catch (IllegalArgumentException ex) {
                domainCode = adminUserService.resolveActorDomainCode(actorUserId);
            }
        } else {
            domainCode = adminUserService.resolveActorDomainCode(actorUserId);
        }

        String selectedRoleCode = roleCode;
        if (userId != null && !StringUtils.hasText(selectedRoleCode)) {
            selectedRoleCode = adminUserService.findPrimaryRoleCode(userId, domainCode);
        }

        if (userId != null) {
            populateEditFormModel(model, userId, domainCode, selectedRoleCode);
        } else {
            populateCreateFormModel(model, domainCode, selectedRoleCode);
        }
    }
    
    private void populateCreateFormModel(Model model, String domainCode, String selectedRoleCode) {
        model.addAttribute("pageTitle", message("admin.users.create.title"));
        model.addAttribute("isEdit", false);
        model.addAttribute("domainCode", domainCode);
        model.addAttribute("formAction", "/admin/users");
        model.addAttribute("submitLabel", message("admin.users.submit.create"));
        populateRoleModel(model, selectedRoleCode);
    }

    private void populateEditFormModel(Model model, Long userId, String domainCode, String selectedRoleCode) {
        model.addAttribute("pageTitle", message("admin.users.edit.title"));
        model.addAttribute("isEdit", true);
        model.addAttribute("userId", userId);
        model.addAttribute("domainCode", domainCode);
        model.addAttribute("formAction", "/admin/users/" + userId);
        model.addAttribute("submitLabel", message("admin.users.submit.edit"));
        populateRoleModel(model, selectedRoleCode);
    }

    private void populateRoleModel(Model model, String selectedRoleCode) {
        var roles = adminUserService.listRoles()
            .stream()
            .map(ability -> new RoleOption(
                ability.getCode(),
                ability.getName(),
                ability.getPermissions().stream()
                    .sorted(java.util.Comparator.comparing(com.cookiesstore.common.authorization.domain.Permission::getCode))
                    .map(permission -> permission.getResource() + ":" + permission.getAction())
                    .toList()
            ))
            .toList();

        model.addAttribute("roles", roles);
        model.addAttribute("selectedRoleCode", selectedRoleCode);
    }


    public record RoleOption(String code, String name, java.util.List<String> permissions) {
    }
}
