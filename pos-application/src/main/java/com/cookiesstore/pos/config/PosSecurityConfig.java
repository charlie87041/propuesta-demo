package com.cookiesstore.pos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class PosSecurityConfig {

    private final PosSessionAuthenticationFilter sessionAuthenticationFilter;

    public PosSecurityConfig(PosSessionAuthenticationFilter sessionAuthenticationFilter) {
        this.sessionAuthenticationFilter = sessionAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain posSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/*/pos/login").permitAll()
                .requestMatchers("/*/pos/logout").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                String redirectTarget = extractPosLoginRedirect(request.getRequestURI());
                response.sendRedirect(redirectTarget);
            }))
            .build();
    }

    private String extractPosLoginRedirect(String path) {
        if (path == null || path.isBlank()) {
            return "/1/pos/login";
        }
        String[] parts = path.split("/");
        if (parts.length > 1 && !parts[1].isBlank()) {
            return "/" + parts[1] + "/pos/login";
        }
        return "/1/pos/login";
    }
}
