package com.cookiesstore.admin.web.controllers.pos;

import com.cookiesstore.admin.service.sources.SourceService;
import com.cookiesstore.admin.service.sources.pos.SourcePosConfigService;
import com.cookiesstore.admin.service.sources.pos.SourcePosUserDomainException;
import com.cookiesstore.admin.service.sources.pos.SourcePosUserService;
import com.cookiesstore.admin.web.dto.sources.pos.AddPosUserForm;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SourcePosUserController {

    private final SourcePosUserService sourcePosUserService;
    private final SourcePosConfigService sourcePosConfigService;
    private final SourceService sourceService;
    private final MessageSource messageSource;

    public SourcePosUserController(
        SourcePosUserService sourcePosUserService,
        SourcePosConfigService sourcePosConfigService,
        SourceService sourceService,
        MessageSource messageSource
    ) {
        this.sourcePosUserService = sourcePosUserService;
        this.sourcePosConfigService = sourcePosConfigService;
        this.sourceService = sourceService;
        this.messageSource = messageSource;
    }

    @GetMapping(
        value = "/admin/product-sources/{sourceId}/manage/pos-users",
        produces = "text/html",
        name = "admin.product-sources.manage.pos-users.list"
    )
    public String list(@PathVariable("sourceId") Long sourceId, Model model) {
        boolean posEnabled = sourcePosConfigService.getOrCreateConfig(sourceId).isPosEnabled();
        model.addAttribute("source", sourceService.getSource(sourceId));
        model.addAttribute("manageSection", "pos-users");
        model.addAttribute("posEnabled", posEnabled);
        model.addAttribute("assignedUsers", sourcePosUserService.listAssignedUsers(sourceId));
        model.addAttribute("availableUsers", sourcePosUserService.listAvailableUsers(sourceId));
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new AddPosUserForm());
        }
        if (!posEnabled) {
            model.addAttribute("errorMessage", message("admin.product_sources.manage.pos.users.error.posDisabled"));
        }
        return "backoffice/product-sources/manage/pos_users_list";
    }

    @PostMapping(
        value = "/admin/product-sources/{sourceId}/manage/pos-users",
        produces = "text/html",
        name = "admin.product-sources.manage.pos-users.create"
    )
    public String assign(
        @PathVariable("sourceId") Long sourceId,
        @Valid @ModelAttribute("form") AddPosUserForm form,
        BindingResult bindingResult,
        @ModelAttribute("currentUserId") Long currentUserId,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        boolean posEnabled = sourcePosConfigService.getOrCreateConfig(sourceId).isPosEnabled();
        if (!posEnabled) {
            redirectAttributes.addFlashAttribute("errorMessage", message("admin.product_sources.manage.pos.users.error.posDisabled"));
            return "redirect:/admin/product-sources/" + sourceId + "/manage/pos-users";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("source", sourceService.getSource(sourceId));
            model.addAttribute("manageSection", "pos-users");
            model.addAttribute("posEnabled", true);
            model.addAttribute("assignedUsers", sourcePosUserService.listAssignedUsers(sourceId));
            model.addAttribute("availableUsers", sourcePosUserService.listAvailableUsers(sourceId));
            return "backoffice/product-sources/manage/pos_users_list";
        }

        try {
            sourcePosUserService.assignUser(sourceId, form.getAdminUserId(), form.getRole(), currentUserId);
            redirectAttributes.addFlashAttribute("successMessage", message("admin.product_sources.manage.pos.users.flash.created"));
        } catch (SourcePosUserDomainException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", message("admin.product_sources.manage.pos.users.error.generic"));
        }
        return "redirect:/admin/product-sources/" + sourceId + "/manage/pos-users";
    }

    @PostMapping(
        value = "/admin/product-sources/{sourceId}/manage/pos-users/{relationId}/delete",
        produces = "text/html",
        name = "admin.product-sources.manage.pos-users.delete"
    )
    public String remove(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("relationId") Long relationId,
        RedirectAttributes redirectAttributes
    ) {
        boolean posEnabled = sourcePosConfigService.getOrCreateConfig(sourceId).isPosEnabled();
        if (!posEnabled) {
            redirectAttributes.addFlashAttribute("errorMessage", message("admin.product_sources.manage.pos.users.error.posDisabled"));
            return "redirect:/admin/product-sources/" + sourceId + "/manage/pos-users";
        }

        try {
            sourcePosUserService.removeUser(sourceId, relationId);
            redirectAttributes.addFlashAttribute("successMessage", message("admin.product_sources.manage.pos.users.flash.deleted"));
        } catch (SourcePosUserDomainException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", message("admin.product_sources.manage.pos.users.error.generic"));
        }
        return "redirect:/admin/product-sources/" + sourceId + "/manage/pos-users";
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
