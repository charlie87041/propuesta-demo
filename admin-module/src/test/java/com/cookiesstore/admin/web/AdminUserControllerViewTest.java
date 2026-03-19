package com.cookiesstore.admin.web;

import com.cookiesstore.admin.domain.AdminUser;
import com.cookiesstore.admin.service.AdminUserService;
import com.cookiesstore.admin.web.controllers.AdminUserViewController;
import com.cookiesstore.admin.web.interceptos.AdminUserFormModelAdvice;
import com.cookiesstore.common.authorization.domain.Ability;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class AdminUserControllerViewTest {

    private final AdminUserService adminUserService = Mockito.mock(AdminUserService.class);
    private final MessageSource messageSource = messageSource();

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
        new AdminUserViewController(adminUserService, messageSource)
    ).setControllerAdvice(
        new AdminUserFormModelAdvice(adminUserService, messageSource)
    ).build();

    @Test
    void shouldRenderUserManagementListView() throws Exception {
        Ability role = new Ability();
        role.setCode("manage-users");
        role.setName("Manage Users");

        AdminUser user = Mockito.mock(AdminUser.class);
        when(user.getId()).thenReturn(1L);
        when(user.getEmail()).thenReturn("admin@cookies.dev");

        mockAuthenticatedUser(99L);
        when(adminUserService.resolveActorDomainCode(99L)).thenReturn("example.test");
        when(adminUserService.listAdminUsersByDomain("example.test")).thenReturn(List.of(user));
        when(adminUserService.findPrimaryRoleCode(1L, "example.test")).thenReturn("manage-users");

        mockMvc.perform(get("/admin/users"))
            .andExpect(status().isOk())
            .andExpect(view().name("backoffice/users/index"))
            .andExpect(model().attributeExists("users"))
            .andExpect(model().attribute("domainCode", "example.test"));
    }

    @Test
    void shouldRenderCreateUserFormView() throws Exception {
        Ability role = new Ability();
        role.setCode("manage-users");
        role.setName("Manage Users");

        mockAuthenticatedUser(99L);
        when(adminUserService.resolveActorDomainCode(99L)).thenReturn("example.test");
        when(adminUserService.listRoles()).thenReturn(List.of(role));

        mockMvc.perform(get("/admin/users/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("backoffice/users/form"))
            .andExpect(model().attribute("pageTitle", "Crear usuario admin"))
            .andExpect(model().attribute("isEdit", false))
            .andExpect(model().attribute("domainCode", "example.test"))
            .andExpect(model().attributeExists("roles"));
    }

    @Test
    void shouldCreateUserAndRedirectToList() throws Exception {
        Ability role = new Ability();
        role.setCode("manage-users");
        role.setName("Manage Users");

        mockAuthenticatedUser(99L);
        when(adminUserService.resolveActorDomainCode(99L)).thenReturn("example.test");
        when(adminUserService.listRoles()).thenReturn(List.of(role));

        mockMvc.perform(post("/admin/users")
                .param("email", "created@cookies.dev")
                .param("password", "Secret123!")
                .param("roleCode", "manage-users"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/users"));

        verify(adminUserService).createAdminUserWithRole(99L, "created@cookies.dev", "Secret123!", "manage-users");
    }

    @Test
    void shouldRenderEditUserFormView() throws Exception {
        Ability role = new Ability();
        role.setCode("manage-users");
        role.setName("Manage Users");

        AdminUser user = new AdminUser();
        user.setEmail("edit@cookies.dev");

        mockAuthenticatedUser(99L);
        when(adminUserService.getAdminUser(42L)).thenReturn(user);
        when(adminUserService.resolveUserDomainCode(42L)).thenReturn("example.test");
        when(adminUserService.findPrimaryRoleCode(42L, "example.test")).thenReturn("manage-users");
        when(adminUserService.listRoles()).thenReturn(List.of(role));

        mockMvc.perform(get("/admin/users/42/edit"))
            .andExpect(status().isOk())
            .andExpect(view().name("backoffice/users/form"))
            .andExpect(model().attribute("pageTitle", "Editar usuario admin"))
            .andExpect(model().attribute("isEdit", true))
            .andExpect(model().attribute("userId", 42L))
            .andExpect(model().attribute("domainCode", "example.test"))
            .andExpect(model().attribute("selectedRoleCode", "manage-users"))
            .andExpect(model().attribute("email", "edit@cookies.dev"));
    }

    @Test
    void shouldUpdateUserAndRedirectToList() throws Exception {
        Ability role = new Ability();
        role.setCode("manage-users");
        role.setName("Manage Users");

        mockAuthenticatedUser(99L);
        when(adminUserService.resolveUserDomainCode(42L)).thenReturn("example.test");
        when(adminUserService.listRoles()).thenReturn(List.of(role));

        mockMvc.perform(post("/admin/users/42")
                .param("email", "updated@cookies.dev")
                .param("password", "NewSecret123!")
                .param("roleCode", "manage-users"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/users"));

        verify(adminUserService).updateAdminUserWithRole(99L, 42L, "updated@cookies.dev", "NewSecret123!", "manage-users");
    }

    @Test
    void shouldDeactivateUserAndRedirectToList() throws Exception {
        mockMvc.perform(post("/admin/users/42/deactivate"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/users"));

        verify(adminUserService).deactivateAdminUser(42L);
    }

    private void mockAuthenticatedUser(Long userId) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(String.valueOf(userId), null));
        LocaleContextHolder.setLocale(java.util.Locale.ENGLISH);
    }
    private MessageSource messageSource() {
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("admin.users.title", java.util.Locale.ENGLISH, "Admin User Management");
        messageSource.addMessage("admin.users.create.title", java.util.Locale.ENGLISH, "Crear usuario admin");
        messageSource.addMessage("admin.users.edit.title", java.util.Locale.ENGLISH, "Editar usuario admin");
        messageSource.addMessage("admin.users.submit.create", java.util.Locale.ENGLISH, "Save User");
        messageSource.addMessage("admin.users.submit.edit", java.util.Locale.ENGLISH, "Save Changes");
        messageSource.addMessage("admin.users.flash.created", java.util.Locale.ENGLISH, "Admin user created successfully.");
        messageSource.addMessage("admin.users.flash.updated", java.util.Locale.ENGLISH, "Admin user updated successfully.");
        messageSource.addMessage("admin.users.flash.deactivated", java.util.Locale.ENGLISH, "Admin user deactivated.");
        return messageSource;
    }
}
