package com.cookiesstore.admin.web.advice.model;

import com.cookiesstore.admin.service.users.AdminUserService;
import com.cookiesstore.admin.service.customers.CustomerAddressService;
import com.cookiesstore.admin.web.advice.support.BaseAdviceSupport;
import com.cookiesstore.admin.web.controllers.AdminCustomerViewController;
import com.cookiesstore.admin.web.controllers.CustomerAddressController;
import com.cookiesstore.admin.web.dto.customers.CustomerAddressForm;
import com.cookiesstore.common.entities.CustomerAddress.AddressType;
import java.util.List;
import org.springframework.context.MessageSource;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@ControllerAdvice(assignableTypes = {AdminCustomerViewController.class, CustomerAddressController.class})
public class AdminCustomerFormModelAdvice extends BaseAdviceSupport {

    private final AdminUserService adminUserService;
    private final CustomerAddressService customerAddressService;

    public AdminCustomerFormModelAdvice(
        AdminUserService adminUserService,
        CustomerAddressService customerAddressService,
        MessageSource messageSource
    ) {
        super(messageSource);
        this.adminUserService = adminUserService;
        this.customerAddressService = customerAddressService;
    }

    @ModelAttribute
    public void populateAdminCustomerFormModel(
        Model model,
        @PathVariable(value = "customerId", required = false) Long customerId,
        @RequestParam(value = "sort", required = false) List<String> sortParams,
        @RequestParam(value = "q", required = false) String searchQuery,
        NativeWebRequest webRequest
    ) {

        Long actorUserId = currentUserId();
        model.addAttribute("currentUserId", actorUserId);
        String routeName = resolveRouteName(webRequest);
        if ("admin.customers.list".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.customers.title"));
            model.addAttribute("activeNav", "customers");
            model.addAttribute("sortParams", sortParams);
            model.addAttribute("searchQuery", searchQuery);
            return;
        }
        if ("admin.customers.create.view".equals(routeName) || "admin.customers.create".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.customers.create"));
            model.addAttribute("activeNav", "customers");
            model.addAttribute("isEdit", false);
            return;
        }
        if ("admin.customers.edit.view".equals(routeName) || "admin.customers.update".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.customers.edit.title"));
            model.addAttribute("activeNav", "customers");
            model.addAttribute("isEdit", true);
            model.addAttribute("customerId", customerId);
            if (customerId != null) {
                if (!model.containsAttribute("customerAddresses")) {
                    model.addAttribute("customerAddresses", customerAddressService.findLatestByCustomerId(customerId));
                }
                if (!model.containsAttribute("addressForm")) {
                    model.addAttribute("addressForm", new CustomerAddressForm(
                        "",
                        "",
                        null,
                        null,
                        "",
                        null,
                        "",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        AddressType.SHIPPING,
                        false
                    ));
                }
                model.addAttribute("addressTypes", AddressType.values());
            }
        }
        
    }

}
