package com.cookiesstore.admin.web.controllers;

import com.cookiesstore.admin.service.AdminAuthenticationService;
import com.cookiesstore.common.auth.AuthCookieNames;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminAuthController {

    private final AdminAuthenticationService adminAuthenticationService;

    public AdminAuthController(AdminAuthenticationService adminAuthenticationService) {
        this.adminAuthenticationService = adminAuthenticationService;
    }

    @GetMapping("/admin/login")
    public String loginView() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/admin";
        }

        return "backoffice/login";
    }

    @PostMapping("/admin/login")
    public String login(
        @RequestParam("email") String email,
        @RequestParam("password") String password,
        HttpServletResponse response
    ) {
        return adminAuthenticationService.authenticate(email, password)
            .map(token -> {
                ResponseCookie cookie = ResponseCookie.from(AuthCookieNames.ADMIN_AUTH_TOKEN, token)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(Duration.ofHours(8))
                    .sameSite("Lax")
                    .build();
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
                return "redirect:/admin";
            })
            .orElse("redirect:/admin/login?error");
    }

    @PostMapping("/admin/logout")
    public String logout(HttpServletResponse response) {
        ResponseCookie cleared = ResponseCookie.from(AuthCookieNames.ADMIN_AUTH_TOKEN, "")
            .httpOnly(true)
            .path("/")
            .maxAge(0)
            .sameSite("Lax")
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cleared.toString());
        SecurityContextHolder.clearContext();
        return "redirect:/admin/login?logout";
    }
}
