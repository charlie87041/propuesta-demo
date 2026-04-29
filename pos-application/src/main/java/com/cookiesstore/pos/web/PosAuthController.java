package com.cookiesstore.pos.web;

import com.cookiesstore.pos.auth.PosAuthenticationService;
import com.cookiesstore.pos.auth.PosAuthenticationStatus;
import com.cookiesstore.pos.auth.PosSessionKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PosAuthController {

    private final PosAuthenticationService posAuthenticationService;

    public PosAuthController(PosAuthenticationService posAuthenticationService) {
        this.posAuthenticationService = posAuthenticationService;
    }

    @GetMapping("/{sourceId}/pos/login")
    public String loginView(@PathVariable("sourceId") Long sourceId, Model model) {
        model.addAttribute("sourceId", sourceId);
        return "pos/login";
    }

    @PostMapping("/{sourceId}/pos/login")
    public String login(
        @PathVariable("sourceId") Long sourceId,
        @RequestParam("email") String email,
        @RequestParam("password") String password,
        HttpServletRequest request
    ) {
        var authResult = posAuthenticationService.authenticate(sourceId, email, password);
        if (authResult.status() == PosAuthenticationStatus.SUCCESS) {
            var principal = authResult.principal();
            HttpSession session = request.getSession(true);
            session.setAttribute(PosSessionKeys.POS_AUTH_USER_ID, principal.userId());
            session.setAttribute(PosSessionKeys.POS_AUTH_SOURCE_ID, principal.sourceId());
            session.setAttribute(PosSessionKeys.POS_AUTH_EMAIL, principal.email());
            return "redirect:/" + sourceId + "/pos";
        }
        if (authResult.status() == PosAuthenticationStatus.POS_DISABLED) {
            return "redirect:/" + sourceId + "/pos/login?posDisabled";
        }
        return "redirect:/" + sourceId + "/pos/login?error";
    }

    @PostMapping("/{sourceId}/pos/logout")
    public String logout(@PathVariable("sourceId") Long sourceId, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/" + sourceId + "/pos/login?logout";
    }
}
