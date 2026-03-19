package com.cookiesstore.admin.web.controllers;

import com.cookiesstore.admin.service.CustomerAvatarStorageService;
import com.cookiesstore.admin.web.dto.users.CreateCustomerForm;
import com.cookiesstore.admin.web.dto.users.UpdateCustomerForm;
import com.cookiesstore.common.entities.Customer;
import com.cookiesstore.common.services.CustomerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminCustomerViewController {

    private static final Logger log = LoggerFactory.getLogger(AdminCustomerViewController.class);

    private final CustomerService customerService;
    private final CustomerAvatarStorageService customerAvatarStorageService;
    private final MessageSource messageSource;

    public AdminCustomerViewController(
        CustomerService customerService,
        CustomerAvatarStorageService customerAvatarStorageService,
        MessageSource messageSource
    ) {
        this.customerService = customerService;
        this.customerAvatarStorageService = customerAvatarStorageService;
        this.messageSource = messageSource;
    }

    @GetMapping(value = "/admin/customers", name = "admin.customers.list")
    public String listCustomers(Model model) {
        var customers = customerService.listCustomers();
        model.addAttribute("customers", customers);
        return "backoffice/customers/index";
    }

    @GetMapping(value = "/admin/customers/new", name = "admin.customers.create.view")
    public String newCustomer(Model model) {
        model.addAttribute("form", new CreateCustomerForm("", "", "", "", true));
        model.addAttribute("isEdit", false);
        return "backoffice/customers/form";
    }

    @PostMapping(value = "/admin/customers", name = "admin.customers.create")
    public String createCustomer(
        @Valid @ModelAttribute("form") CreateCustomerForm customerForm,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes,
        @RequestParam(value = "avatar", required = false) MultipartFile avatar,
        @ModelAttribute("currentUserId") Long actorUserId
    ) {
        model.addAttribute("isEdit", false);
        if (bindingResult.hasErrors()) {
            return "backoffice/customers/form";
        }

        Customer created;
        try {
            created = customerService.createCustomer(
                customerForm.name(),
                customerForm.email(),
                customerForm.password(),
                customerForm.phone(),
                customerForm.active()
            );
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "backoffice/customers/form";
        }

        if (avatar != null && !avatar.isEmpty()) {
            try {
                String objectKey = customerAvatarStorageService.uploadCustomerAvatar(created.getId(), avatar);
                customerService.updateLogoUrl(created.getId(), objectKey);
            } catch (RuntimeException ex) {
                log.warn("Customer {} created but avatar upload failed: {}", created.getId(), ex.getMessage());
                redirectAttributes.addFlashAttribute("warningMessage", message("admin.customers.flash.avatar.uploadFailed"));
            }
        }

        redirectAttributes.addFlashAttribute("successMessage", message("admin.customers.flash.created"));
        return "redirect:/admin/customers";
    }

    @GetMapping(value = "/admin/customers/{customerId}/edit", name = "admin.customers.edit.view")
    public String editCustomer(
        @PathVariable("customerId") Long customerId,
        Model model
    ) {
        Customer customer = customerService.findById(customerId).orElse(null);
        if (customer == null) {
            return "redirect:/admin/customers";
        }
        model.addAttribute("form", new UpdateCustomerForm(customer.getName(), customer.getEmail(), customer.getPhone(), customer.isActive()));
        model.addAttribute("isEdit", true);
        model.addAttribute("customerId", customerId);
        return "backoffice/customers/form";
    }

    @PostMapping(value = "/admin/customers/{customerId}", name = "admin.customers.update")
    public String updateCustomer(
        @PathVariable("customerId") Long customerId,
        @Valid @ModelAttribute("form") UpdateCustomerForm customerForm,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes,
        @RequestParam(value = "avatar", required = false) MultipartFile avatar
    ) {
        model.addAttribute("isEdit", true);
        model.addAttribute("customerId", customerId);

        if (bindingResult.hasErrors()) {
            return "backoffice/customers/form";
        }

        try {
            boolean updated = customerService.updateCustomer(
                customerId,
                customerForm.name(),
                customerForm.email(),
                customerForm.phone(),
                customerForm.active()
            );
            if (!updated) {
                redirectAttributes.addFlashAttribute("errorMessage", message("admin.customers.flash.notFound"));
                return "redirect:/admin/customers";
            }
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "backoffice/customers/form";
        }

        if (avatar != null && !avatar.isEmpty()) {
            try {
                String objectKey = customerAvatarStorageService.uploadCustomerAvatar(customerId, avatar);
                customerService.updateLogoUrl(customerId, objectKey);
            } catch (RuntimeException ex) {
                log.warn("Customer {} updated but avatar upload failed: {}", customerId, ex.getMessage());
                redirectAttributes.addFlashAttribute("warningMessage", message("admin.customers.flash.avatar.updateFailed"));
            }
        }

        redirectAttributes.addFlashAttribute("successMessage", message("admin.customers.flash.updated"));
        return "redirect:/admin/customers";
    }

    @PostMapping(value = "/admin/customers/{customerId}/deactivate", name = "admin.customers.deactivate")
    public String deactivateCustomer(
        @PathVariable("customerId") Long customerId,
        RedirectAttributes redirectAttributes
    ) {
        boolean deactivated = customerService.disableCustomer(customerId);
        if (deactivated) {
            redirectAttributes.addFlashAttribute("successMessage", message("admin.customers.flash.deactivated"));
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", message("admin.customers.flash.notFound"));
        }
        return "redirect:/admin/customers";
    }

    @PostMapping(value = "/admin/customers/{customerId}/enable", name = "admin.customers.enable")
    public String enableCustomer(
        @PathVariable("customerId") Long customerId,
        RedirectAttributes redirectAttributes
    ) {
        boolean enabled = customerService.enableCustomer(customerId);
        if (enabled) {
            redirectAttributes.addFlashAttribute("successMessage", message("admin.customers.flash.enabled"));
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", message("admin.customers.flash.notFound"));
        }
        return "redirect:/admin/customers";
    }

    @GetMapping("/admin/customers/{customerId}/avatar")
    public ResponseEntity<InputStreamResource> avatar(@PathVariable("customerId") Long customerId) {
        Customer customer = customerService.findById(customerId).orElse(null);
        if (customer == null || customer.getLogoUrl() == null || customer.getLogoUrl().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        try {
            var avatarObject = customerAvatarStorageService.loadAvatar(customer.getLogoUrl());
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            if (avatarObject.contentType() != null && !avatarObject.contentType().isBlank()) {
                mediaType = MediaType.parseMediaType(avatarObject.contentType());
            }

            return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=300")
                .body(new InputStreamResource(avatarObject.stream()));
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
