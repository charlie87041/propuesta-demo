package com.cookiesstore.admin.web.controllers;

import com.cookiesstore.admin.domain.AdminUser;
import com.cookiesstore.admin.service.users.AdminUserService;
import com.cookiesstore.admin.web.dto.users.CreateAdminUserForm;
import com.cookiesstore.admin.web.dto.users.UpdateAdminUserForm;

import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminUserViewController {

    private final AdminUserService adminUserService;
    private final MessageSource messageSource;

    public AdminUserViewController(AdminUserService adminUserService, MessageSource messageSource) {
        this.adminUserService = adminUserService;
        this.messageSource = messageSource;
    }

    @GetMapping(value = "/admin/users", name = "admin.users.list")
    public String usersList(
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        @ModelAttribute("searchQuery") String searchQuery,
        Model model,
        @ModelAttribute("currentUserId") Long actorUserId
    ) {
        String domainCode = adminUserService.resolveActorDomainCode(actorUserId);

        var usersPage = adminUserService.listAdminUsersByDomain(domainCode, pageable, searchQuery);
        Map<Long, String> userRoles = new LinkedHashMap<>();
        for (AdminUser user : usersPage.getContent()) {
            userRoles.put(user.getId(), adminUserService.findPrimaryRoleCode(user.getId(), domainCode));
        }

        model.addAttribute("usersPage", usersPage);
        model.addAttribute("userRoles", userRoles);
        model.addAttribute("domainCode", domainCode);
        return "backoffice/users/index";
    }

    @GetMapping(value = "/admin/users/new", name = "admin.users.create")
    public String createUserView(Model model) {
        return "backoffice/users/form";
    }

    @PostMapping(value = "/admin/users", name = "admin.users.store")
    public String createUser(
        @Valid @ModelAttribute("form") CreateAdminUserForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes,
        @ModelAttribute("currentUserId") Long actorUserId
    ) {
        model.addAttribute("email", form.email());

        if (bindingResult.hasErrors()) {
            applyFieldErrors(model, bindingResult);
            return "backoffice/users/form";
        }
        AdminUser created = adminUserService.createAdminUserWithRole(
            actorUserId,
            form.email(),
            form.password(),
            form.roleCode()
        );
        applyPermissionOverrides(actorUserId, created.getId(), form, adminUserService.resolveUserDomainCode(created.getId()));
        redirectAttributes.addFlashAttribute("successMessage", message("admin.users.flash.created"));
        return "redirect:/admin/users";
    }

    @GetMapping(value = "/admin/users/{userId}/edit", name = "admin.users.edit")
    public String editUserView(@PathVariable("userId") Long userId, Model model, RedirectAttributes redirectAttributes) {
        AdminUser user = adminUserService.getAdminUser(userId);
        model.addAttribute("email", user.getEmail());
        return "backoffice/users/form";
    }

    @PostMapping(value = "/admin/users/{userId}", name = "admin.users.update")
    public String updateUser(
        @PathVariable("userId") Long userId,
        @Valid @ModelAttribute("form") UpdateAdminUserForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes,
        @ModelAttribute("currentUserId") Long actorUserId
    ) {
        model.addAttribute("email", form.email());

        if (bindingResult.hasErrors()) {
            applyFieldErrors(model, bindingResult);
            return "backoffice/users/form";
        }

        adminUserService.updateAdminUserWithRole(
            actorUserId,
            userId,
            form.email(),
            form.password(),
            form.roleCode()
        );
        applyPermissionOverrides(actorUserId, userId, form, adminUserService.resolveUserDomainCode(userId));
        redirectAttributes.addFlashAttribute("successMessage", message("admin.users.flash.updated"));
        return "redirect:/admin/users";
    }

    @PostMapping(value = "/admin/users/{userId}/deactivate", name = "admin.users.deactivate")
    public String deactivateUserFromView(
        @PathVariable("userId") Long userId,
         RedirectAttributes redirectAttributes,
        @ModelAttribute("currentUserId") Long actorUserId
        ) {
        if (userId == actorUserId) {
            redirectAttributes.addFlashAttribute("errorMessage", message("admin.users.error.deactivate.self"));
            return "redirect:/admin/users";
            
        }
        adminUserService.deactivateAdminUser(userId);
        redirectAttributes.addFlashAttribute("successMessage", message("admin.users.flash.deactivated"));
        return "redirect:/admin/users";
    }



    @PostMapping(value = "/admin/users/{userId}/enable", name = "admin.users.enable")
    public String enableUserFromView(@PathVariable("userId") Long userId, RedirectAttributes redirectAttributes) {
        adminUserService.enableAdminUser(userId);
        redirectAttributes.addFlashAttribute("successMessage", message("admin.users.flash.enabled"));
        return "redirect:/admin/users";
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    private void applyFieldErrors(Model model, BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors("email")) {
            model.addAttribute("emailError", bindingResult.getFieldError("email").getDefaultMessage());
        }
        if (bindingResult.hasFieldErrors("password")) {
            model.addAttribute("passwordError", bindingResult.getFieldError("password").getDefaultMessage());
        }
        if (bindingResult.hasFieldErrors("roleCode")) {
            model.addAttribute("roleError", bindingResult.getFieldError("roleCode").getDefaultMessage());
        }
    }

    private void applyPermissionOverrides(
        Long actorUserId,
        Long targetUserId,
        CreateAdminUserForm form,
        String domainCode
    ) {
        Set<String> permissionCodes = toSet(form.permissionCodes());
        if (permissionCodes.isEmpty()) {
            return;
        }
        Set<String> grantedPermissions = toSet(form.grantedPermissions());
        Set<String> deniedPermissions = new LinkedHashSet<>();
        for (String permissionCode : permissionCodes) {
            if (!grantedPermissions.contains(permissionCode)) {
                deniedPermissions.add(permissionCode);
            }
        }
        adminUserService.syncPermissionOverrides(
            actorUserId,
            targetUserId,
            domainCode,
            permissionCodes,
            deniedPermissions
        );
    }

    private void applyPermissionOverrides(
        Long actorUserId,
        Long targetUserId,
        UpdateAdminUserForm form,
        String domainCode
    ) {
        Set<String> permissionCodes = toSet(form.permissionCodes());
        if (permissionCodes.isEmpty()) {
            return;
        }
        Set<String> grantedPermissions = toSet(form.grantedPermissions());
        Set<String> deniedPermissions = new LinkedHashSet<>();
        for (String permissionCode : permissionCodes) {
            if (!grantedPermissions.contains(permissionCode)) {
                deniedPermissions.add(permissionCode);
            }
        }
        adminUserService.syncPermissionOverrides(
            actorUserId,
            targetUserId,
            domainCode,
            permissionCodes,
            deniedPermissions
        );
    }

    private Set<String> toSet(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        return new LinkedHashSet<>(values);
    }
}
