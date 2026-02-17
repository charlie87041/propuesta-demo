package com.cookiesstore.admin.web;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class AdminUserViewControllerTest {

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminUserViewController()).build();

    @Test
    void shouldRenderUserManagementListView() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("backoffice/users/index"));
    }

    @Test
    void shouldRenderCreateUserFormView() throws Exception {
        mockMvc.perform(get("/admin/users/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("backoffice/users/form"))
                .andExpect(model().attribute("pageTitle", "Crear usuario admin"));
    }

    @Test
    void shouldRenderEditUserFormView() throws Exception {
        mockMvc.perform(get("/admin/users/42/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("backoffice/users/form"))
                .andExpect(model().attribute("pageTitle", "Editar usuario admin"))
                .andExpect(model().attribute("userId", 42L));
    }
}
