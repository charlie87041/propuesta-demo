package com.cookiesstore.common.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cookiesstore.common.auth.JwtTokenProvider;
import com.cookiesstore.common.config.CommonConfiguration;
import com.cookiesstore.common.security.JwtAuthenticationFilter;
import com.cookiesstore.common.security.SecurityConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(
    classes = ApiResponseWrapperIntegrationTest.ApiTestApplication.class,
    properties = {
        "security.jwt.secret=this-is-a-test-secret-key-with-at-least-32-bytes-long-1234567890",
        "security.jwt.expiration=PT1H",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
    }
)
@AutoConfigureMockMvc
class ApiResponseWrapperIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void successResponseFormatIsStandardized() throws Exception {
        mockMvc.perform(get("/public/api/success").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.message").value("ok"))
            .andExpect(jsonPath("$.error").doesNotExist())
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void errorResponseFormatIsStandardized() throws Exception {
        mockMvc.perform(get("/public/api/error").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.data").doesNotExist())
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.error.message").value("invalid input"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void validationErrorResponseIncludesFieldDetails() throws Exception {
        mockMvc.perform(post("/public/api/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.message").value("Validation failed"))
            .andExpect(jsonPath("$.error.details").isArray())
            .andExpect(jsonPath("$.error.details[0].field").value("name"));
    }

    @SpringBootApplication
    @Import({
        CommonConfiguration.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtTokenProvider.class,
        GlobalExceptionHandler.class,
        ApiResponseWrapperIntegrationTest.ApiTestController.class
    })
    static class ApiTestApplication {
    }

    @RestController
    @RequestMapping("/public/api")
    static class ApiTestController {

        @GetMapping("/success")
        public ApiResponse<MessageResponse> success() {
            return ApiResponse.success(new MessageResponse("ok"));
        }

        @GetMapping("/error")
        public ApiResponse<Void> error() {
            throw new IllegalArgumentException("invalid input");
        }

        @PostMapping("/validate")
        public ApiResponse<MessageResponse> validate(@Valid @RequestBody NameRequest request) {
            return ApiResponse.success(new MessageResponse(request.name()));
        }
    }

    record MessageResponse(String message) {
    }

    record NameRequest(@NotBlank String name) {
    }
}
