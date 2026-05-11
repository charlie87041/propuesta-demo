package com.cookiesstore.infra.auth.web;

import com.cookiesstore.infra.auth.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;

@Controller
public class AuthViewController {

    private final AuthenticationService authenticationService;

    public AuthViewController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String loginView(
        @RequestParam(name = "next", required = false) String next,
        @RequestParam(name = "error", required = false) String error,
        Model model
    ) {
        model.addAttribute("next", normalizeNext(next));
        model.addAttribute("error", error);
        return "auth/login";
    }

    @PostMapping("/login")
    public String loginSubmit(
        @RequestParam String username,
        @RequestParam String password,
        @RequestParam(name = "next", required = false) String next,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        try {
            authenticationService.login(username, password, request, response);
            return "redirect:" + normalizeNext(next);
        } catch (BadCredentialsException exception) {
            String encodedNext = UriUtils.encode(normalizeNext(next), StandardCharsets.UTF_8);
            return "redirect:/login?error=invalid-credentials&next=" + encodedNext;
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        authenticationService.logout(request, response);
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard/index";
    }

    @GetMapping("/environments")
    public String environments() {
        return "redirect:/projects";
    }

    private String normalizeNext(String next) {
        if (next == null || next.isBlank() || !next.startsWith("/") || next.startsWith("//")) {
            return "/dashboard";
        }
        return next;
    }
}
