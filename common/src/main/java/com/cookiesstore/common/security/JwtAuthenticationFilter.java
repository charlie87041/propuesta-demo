package com.cookiesstore.common.security;

import com.cookiesstore.common.auth.AuthCookieNames;
import com.cookiesstore.common.auth.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);
        String path = request.getRequestURI();

        if (token != null) {
            if (!jwtTokenProvider.isValid(token)) {
                SecurityContextHolder.clearContext();
                clearAuthCookie(response);

                if (isAdminPath(path) && !"/admin/login".equals(path)) {
                    response.sendRedirect("/admin/login?error");
                    return;
                }

                if ("/admin/login".equals(path)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid JWT token");
                return;
            }

            Long userId = jwtTokenProvider.extractUserId(token);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (AuthCookieNames.ADMIN_AUTH_TOKEN.equals(cookie.getName())
                && cookie.getValue() != null
                && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private boolean isAdminPath(String path) {
        return path != null && path.startsWith("/admin");
    }

    private void clearAuthCookie(HttpServletResponse response) {
        ResponseCookie cleared = ResponseCookie.from(AuthCookieNames.ADMIN_AUTH_TOKEN, "")
            .httpOnly(true)
            .path("/")
            .maxAge(0)
            .sameSite("Lax")
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cleared.toString());
    }
}
