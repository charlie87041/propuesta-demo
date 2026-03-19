package com.cookiesstore.common.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cookiesstore.common.config.CommonConfiguration;
import com.cookiesstore.common.security.JwtAuthenticationFilter;
import com.cookiesstore.common.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(
    classes = SecurityFilterChainIntegrationTest.SecurityTestApplication.class,
    properties = {
        "security.jwt.secret=this-is-a-test-secret-key-with-at-least-32-bytes-long-1234567890",
        "security.jwt.expiration=PT1H",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
    }
)
@AutoConfigureMockMvc
class SecurityFilterChainIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void publicEndpointIsAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/public/ping").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointRequiresToken() throws Exception {
        mockMvc.perform(get("/protected/ping").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointReturnsUnauthorizedForInvalidToken() throws Exception {
        mockMvc.perform(get("/protected/ping")
                .header("Authorization", "Bearer invalid-token")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointIsAccessibleWithValidToken() throws Exception {
        String token = jwtTokenProvider.generateToken(1001L);

        mockMvc.perform(get("/protected/ping")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @SpringBootApplication
    @Import({
        CommonConfiguration.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtTokenProvider.class,
        SecurityFilterChainIntegrationTest.TestController.class
    })
    static class SecurityTestApplication {
    }

    @RestController
    @RequestMapping
    static class TestController {

        @GetMapping("/public/ping")
        public String publicPing() {
            return "ok";
        }

        @GetMapping("/protected/ping")
        public String protectedPing() {
            return "ok";
        }
    }
}
