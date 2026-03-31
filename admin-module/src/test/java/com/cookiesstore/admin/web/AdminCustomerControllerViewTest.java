package com.cookiesstore.admin.web;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.cookiesstore.admin.service.customers.CustomerAvatarStorageService;
import com.cookiesstore.admin.service.users.AdminUserService;
import com.cookiesstore.admin.web.controllers.AdminCustomerViewController;
import com.cookiesstore.admin.web.advice.model.AdminCustomerFormModelAdvice;
import com.cookiesstore.common.entities.Customer;
import com.cookiesstore.common.services.CustomerService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AdminCustomerControllerViewTest {

    private final CustomerService customerService = Mockito.mock(CustomerService.class);
    private final CustomerAvatarStorageService customerAvatarStorageService = Mockito.mock(CustomerAvatarStorageService.class);
    private final AdminUserService adminUserService = Mockito.mock(AdminUserService.class);
    private final MessageSource messageSource = messageSource();

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
        new AdminCustomerViewController(customerService, customerAvatarStorageService, messageSource)
    ).setControllerAdvice(
        new AdminCustomerFormModelAdvice(adminUserService, messageSource)
    ).build();

    @Test
    void shouldRenderCustomerManagementListView() throws Exception {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Cookie Store");
        customer.setEmail("hello@cookies.dev");
        customer.setActive(true);

        mockAuthenticatedUser(99L);
        when(customerService.listCustomers()).thenReturn(List.of(customer));

        mockMvc.perform(get("/admin/customers"))
            .andExpect(status().isOk())
            .andExpect(view().name("backoffice/customers/index"))
            .andExpect(model().attributeExists("customers"))
            .andExpect(model().attribute("pageTitle", "Customer Management"));
    }

    @Test
    void shouldDeactivateCustomerAndRedirectToList() throws Exception {
        mockAuthenticatedUser(99L);
        when(customerService.disableCustomer(42L)).thenReturn(true);

        mockMvc.perform(post("/admin/customers/42/deactivate"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/customers"))
            .andExpect(flash().attribute("successMessage", "Customer deactivated successfully."));

        verify(customerService).disableCustomer(42L);
    }

    @Test
    void shouldEnableCustomerAndRedirectToList() throws Exception {
        mockAuthenticatedUser(99L);
        when(customerService.enableCustomer(42L)).thenReturn(true);

        mockMvc.perform(post("/admin/customers/42/enable"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/customers"))
            .andExpect(flash().attribute("successMessage", "Customer enabled successfully."));

        verify(customerService).enableCustomer(42L);
    }

    @Test
    void shouldShowErrorWhenCustomerDoesNotExistOnDeactivate() throws Exception {
        mockAuthenticatedUser(99L);
        when(customerService.disableCustomer(999L)).thenReturn(false);

        mockMvc.perform(post("/admin/customers/999/deactivate"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/customers"))
            .andExpect(flash().attribute("errorMessage", "Customer not found."));

        verify(customerService).disableCustomer(999L);
    }

    private void mockAuthenticatedUser(Long userId) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(String.valueOf(userId), null));
        LocaleContextHolder.setLocale(java.util.Locale.ENGLISH);
    }

    private MessageSource messageSource() {
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("admin.customers.title", java.util.Locale.ENGLISH, "Customer Management");
        messageSource.addMessage("admin.customers.create", java.util.Locale.ENGLISH, "Create customer");
        messageSource.addMessage("admin.customers.flash.deactivated", java.util.Locale.ENGLISH, "Customer deactivated successfully.");
        messageSource.addMessage("admin.customers.flash.enabled", java.util.Locale.ENGLISH, "Customer enabled successfully.");
        messageSource.addMessage("admin.customers.flash.notFound", java.util.Locale.ENGLISH, "Customer not found.");
        return messageSource;
    }
}
