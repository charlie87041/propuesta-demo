package com.cookiesstore.infra.auth.web;

import com.cookiesstore.common.api.ApiResponse;
import com.cookiesstore.infra.auth.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationService.AuthenticatedUser>> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    ) {
        AuthenticationService.AuthenticatedUser user = authenticationService.login(
            request.username(),
            request.password(),
            httpRequest,
            httpResponse
        );
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        authenticationService.logout(request, response);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthenticationService.AuthenticatedUser>> me(Authentication authentication) {
        AuthenticationService.AuthenticatedUser user = authenticationService.currentUser(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
    ) {
    }
}
