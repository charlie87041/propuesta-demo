package com.cookiesstore.admin.web.controllers;

import com.cookiesstore.admin.web.dto.users.CreateCustomerForm;
import com.cookiesstore.common.authorization.annotation.RequiresAbility;
import com.cookiesstore.common.services.CustomerService;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminCustomerViewController {

    private final CustomerService customerService;
    private final MessageSource messageSource;

    public AdminCustomerViewController(CustomerService customerService, MessageSource messageSource) {
        this.customerService = customerService;
        this.messageSource = messageSource;
    }

    @GetMapping(value = "/admin/customers", name = "admin.customers.list")
    public String listCustomers(
        ModelAndView model
    ) {
        var customers = customerService.listCustomers();
        model.addObject("customers", customers);
        return  "backoffice/customers/index";
    }

    @GetMapping(value = "/admin/customers/new", name = "admin.customers.create.view")
    public String newCustomer(
        Model model
    ) {
        model.addAttribute("form", new CreateCustomerForm("", "", "", "", "", true));
        return "backoffice/customers/form";
    }


    @PostMapping(value = "/admin/customers", name = "admin.customers.create")
    public String createCustomer(
        @Valid @ModelAttribute("form") CreateCustomerForm customerForm,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes,
        @ModelAttribute("currentUserId") Long actorUserId

    ) {
        if (bindingResult.hasErrors()) {
           return "backoffice/customers/form";
        }

        redirectAttributes.addFlashAttribute("successMessage", message("admin.customers.flash.created"));
        return "redirect:/admin/customers";
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

}
