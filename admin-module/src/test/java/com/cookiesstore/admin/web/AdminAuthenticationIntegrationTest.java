package com.cookiesstore.admin.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.cookiesstore.admin.domain.AdminUser;
import com.cookiesstore.admin.repository.AdminUserRepository;
import com.cookiesstore.admin.service.AdminAuthenticationService;
import com.cookiesstore.common.auth.AuthCookieNames;
import com.cookiesstore.common.auth.JwtTokenProvider;
import com.cookiesstore.common.config.CommonConfiguration;
import com.cookiesstore.common.security.JwtAuthenticationFilter;
import com.cookiesstore.common.security.SecurityConfig;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(
    classes = AdminAuthenticationIntegrationTest.TestConfig.class,
    properties = {
        "security.jwt.secret=this-is-a-test-secret-key-with-at-least-32-bytes-long-1234567890",
        "security.jwt.expiration=PT1H",
        "spring.datasource.url=jdbc:h2:mem:adminauth;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    }
)
@AutoConfigureMockMvc
class AdminAuthenticationIntegrationTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = {AdminUser.class})
    @EnableJpaRepositories(basePackageClasses = {AdminUserRepository.class})
    @Import({
        CommonConfiguration.class,
        JwtTokenProvider.class,
        JwtAuthenticationFilter.class,
        SecurityConfig.class,
        AdminBackofficeController.class,
        AdminAuthController.class,
        AdminAuthenticationService.class
    })
    static class TestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @BeforeEach
    void setup() {
        adminUserRepository.deleteAll();

        AdminUser adminUser = new AdminUser();
        adminUser.setEmail("admin@cookies.dev");
        adminUser.setPassword(BCrypt.hashpw("Secret123!", BCrypt.gensalt()));
        adminUser.setActive(true);
        adminUserRepository.save(adminUser);
    }

    @Test
    void loginPageIsPublic() throws Exception {
        mockMvc.perform(get("/admin/login"))
            .andExpect(status().isOk())
            .andExpect(view().name("backoffice/login"));
    }

    @Test
    void loginPageUsesSpanishByDefault() throws Exception {
        mockMvc.perform(get("/admin/login"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Bienvenido de nuevo")));
    }

    
    @Test
    void loginPageUsesEnglishWhenLocaleIsRequested() throws Exception {
        mockMvc.perform(get("/admin/login").param("lang", "en"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Welcome Back")));
    }

    
    @Test
    void adminPageRedirectsToLoginWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/admin"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/login"));
    }

    @Test
    void validLoginSetsCookieAndRedirectsToBackoffice() throws Exception {
        mockMvc.perform(post("/admin/login")
                .param("email", "admin@cookies.dev")
                .param("password", "Secret123!"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin"))
            .andExpect(header().string("Set-Cookie", containsString("ADMIN_AUTH_TOKEN=")));
    }

    @Test
    void invalidLoginRedirectsWithErrorAndNoAuthCookie() throws Exception {
        mockMvc.perform(post("/admin/login")
                .param("email", "admin@cookies.dev")
                .param("password", "wrong-pass"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/login?error"))
            .andExpect(header().doesNotExist("Set-Cookie"));
    }

    @Test
    void authenticatedCookieAllowsAccessToAdminPage() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/admin/login")
                .param("email", "admin@cookies.dev")
                .param("password", "Secret123!"))
            .andReturn();

        Cookie authCookie = loginResult.getResponse().getCookie(AuthCookieNames.ADMIN_AUTH_TOKEN);

        mockMvc.perform(get("/admin").cookie(authCookie))
            .andExpect(status().isOk())
            .andExpect(view().name("backoffice/index"));
    }

    @Test
    void logoutClearsCookieAndRedirectsToLogin() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/admin/login")
                .param("email", "admin@cookies.dev")
                .param("password", "Secret123!"))
            .andReturn();

        Cookie authCookie = loginResult.getResponse().getCookie(AuthCookieNames.ADMIN_AUTH_TOKEN);

        mockMvc.perform(post("/admin/logout").cookie(authCookie))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/login?logout"))
            .andExpect(header().string("Set-Cookie", containsString("ADMIN_AUTH_TOKEN=")))
            .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
    }

    
    @Test
    void invalidCookieOnAdminPageRedirectsToLoginAndClearsCookie() throws Exception {
        mockMvc.perform(get("/admin/users/new").cookie(new Cookie(AuthCookieNames.ADMIN_AUTH_TOKEN, "invalid-token")))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/login?error"))
            .andExpect(header().string("Set-Cookie", containsString("ADMIN_AUTH_TOKEN=")))
            .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
    }
}
