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
import org.springframework.security.access.AccessDeniedException;

@ControllerAdvice(annotations = Controller.class, assignableTypes = AdminUserViewController.class)
public class AdminUserFormModelAdvice {

    private final AdminUserService adminUserService;
    private final MessageSource messageSource;

    public AdminUserFormModelAdvice(AdminUserService adminUserService, MessageSource messageSource) {
        this.adminUserService = adminUserService;
        this.messageSource = messageSource;
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

    private String resolveRouteName(NativeWebRequest webRequest) {
        Object handler = webRequest.getAttribute(
            HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE,
            NativeWebRequest.SCOPE_REQUEST
        );
        if (handler instanceof HandlerMethod handlerMethod) {
            org.springframework.web.bind.annotation.RequestMapping mapping =
                org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), org.springframework.web.bind.annotation.RequestMapping.class);
            if (mapping != null && StringUtils.hasText(mapping.name())) {
                return mapping.name();
            }
        }
        return null;
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

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    private Long currentUserId() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
            .getContext()
            .getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AccessDeniedException("Unauthenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Long userId) {
            return userId;
        }
        if (principal instanceof String textPrincipal) {
            return Long.parseLong(textPrincipal);
        }

        throw new AccessDeniedException("Invalid authentication principal");
    }

    public record RoleOption(String code, String name, java.util.List<String> permissions) {
    }
}
