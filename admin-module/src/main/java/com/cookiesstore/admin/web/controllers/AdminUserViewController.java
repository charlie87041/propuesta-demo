package com.cookiesstore.admin.web.controllers;

import com.cookiesstore.admin.domain.AdminUser;
import com.cookiesstore.admin.service.AdminUserService;
import com.cookiesstore.admin.web.dto.users.CreateAdminUserForm;
import com.cookiesstore.admin.web.dto.users.UpdateAdminUserForm;

import jakarta.validation.Valid;
import com.cookiesstore.admin.web.viewmodels.AdminUserViewModel;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import java.util.List;
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
        Model model,
        @ModelAttribute("currentUserId") Long actorUserId
    ) {
        String domainCode = adminUserService.resolveActorDomainCode(actorUserId);

        List<AdminUserViewModel> users = adminUserService.listAdminUsersByDomain(domainCode)
            .stream()
            .map(user -> AdminUserViewModel.from(
                user,
        adminUserService.findPrimaryRoleCode(user.getId(), domainCode)
            ))
            .toList();

        model.addAttribute("users", users);
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
            return "backoffice/users/form";
        }
        adminUserService.createAdminUserWithRole(actorUserId, form.email(), form.password(), form.roleCode());
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
            return "backoffice/users/form";
        }

        adminUserService.updateAdminUserWithRole(
            actorUserId,
            userId,
            form.email(),
            form.password(),
            form.roleCode()
        );
        redirectAttributes.addFlashAttribute("successMessage", message("admin.users.flash.updated"));
        return "redirect:/admin/users";
    }

    @PostMapping(value = "/admin/users/{userId}/deactivate", name = "admin.users.deactivate")
    public String deactivateUserFromView(@PathVariable("userId") Long userId, RedirectAttributes redirectAttributes) {
        adminUserService.deactivateAdminUser(userId);
        redirectAttributes.addFlashAttribute("successMessage", message("admin.users.flash.deactivated"));
        return "redirect:/admin/users";
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
