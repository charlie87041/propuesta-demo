package com.cookiesstore.admin.web.controllers;

import com.cookiesstore.admin.service.customers.CustomerAddressService;
import com.cookiesstore.admin.web.dto.customers.CustomerAddressForm;
import com.cookiesstore.common.entities.CustomerAddress;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CustomerAddressController {

    private final CustomerAddressService customerAddressService;
    private final MessageSource messageSource;

    public CustomerAddressController(
        CustomerAddressService customerAddressService,
        MessageSource messageSource
    ) {
        this.customerAddressService = customerAddressService;
        this.messageSource = messageSource;
    }

    @GetMapping(value = "/admin/customers/{customerId}/addresses", name = "admin.customers.addresses.list")
    public String listAddresses(
        @PathVariable("customerId") Long customerId,
        RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addFlashAttribute("customerAddresses", customerAddressService.findLatestByCustomerId(customerId));
        return "redirect:/admin/customers/" + customerId + "/edit#customer-addresses";
    }

    @PostMapping(value = "/admin/customers/{customerId}/addresses", name = "admin.customers.addresses.create")
    public String createAddress(
        @PathVariable("customerId") Long customerId,
        @Valid @ModelAttribute("addressForm") CustomerAddressForm form,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", message("admin.customers.addresses.error.invalid"));
            redirectAttributes.addFlashAttribute("addressForm", form);
            return "redirect:/admin/customers/" + customerId + "/edit#customer-addresses";
        }

        customerAddressService.createAddress(customerId, form);
        redirectAttributes.addFlashAttribute("successMessage", message("admin.customers.addresses.flash.created"));
        return "redirect:/admin/customers/" + customerId + "/edit#customer-addresses";
    }

    @PostMapping(value = "/admin/customers/{customerId}/addresses/{addressId}", name = "admin.customers.addresses.update")
    public String updateAddress(
        @PathVariable("customerId") Long customerId,
        @PathVariable("addressId") Long addressId,
        @Valid @ModelAttribute("addressForm") CustomerAddressForm form,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", message("admin.customers.addresses.error.invalid"));
            redirectAttributes.addFlashAttribute("addressForm", form);
            return "redirect:/admin/customers/" + customerId + "/edit#customer-addresses";
        }

        CustomerAddress updated = customerAddressService.updateAddress(customerId, addressId, form);
        if (updated.getId().equals(addressId)) {
            redirectAttributes.addFlashAttribute("warningMessage", message("admin.customers.addresses.flash.noChanges"));
        } else {
            redirectAttributes.addFlashAttribute("successMessage", message("admin.customers.addresses.flash.updated"));
        }
        redirectAttributes.addFlashAttribute("updatedAddressId", updated.getId());
        return "redirect:/admin/customers/" + customerId + "/edit#customer-addresses";
    }

    @PostMapping(value = "/admin/customers/{customerId}/addresses/{addressId}/delete", name = "admin.customers.addresses.delete")
    public String deleteAddress(
        @PathVariable("customerId") Long customerId,
        @PathVariable("addressId") Long addressId,
        RedirectAttributes redirectAttributes
    ) {
        customerAddressService.deleteAddress(customerId, addressId);
        redirectAttributes.addFlashAttribute("successMessage", message("admin.customers.addresses.flash.deleted"));
        return "redirect:/admin/customers/" + customerId + "/edit#customer-addresses";
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
