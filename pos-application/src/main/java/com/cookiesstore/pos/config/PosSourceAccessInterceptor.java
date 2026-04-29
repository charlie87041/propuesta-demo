package com.cookiesstore.pos.config;

import com.cookiesstore.pos.auth.PosPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PosSourceAccessInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        if (path == null || !path.matches("^/\\d+/pos(?:/.*)?$")) {
            return true;
        }
        if (path.endsWith("/login")) {
            return true;
        }

        Long pathSourceId = extractSourceId(path);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof PosPrincipal principal)) {
            response.sendRedirect("/" + pathSourceId + "/pos/login");
            return false;
        }

        if (!pathSourceId.equals(principal.sourceId())) {
            response.sendRedirect("/" + principal.sourceId() + "/pos");
            return false;
        }

        return true;
    }

    private Long extractSourceId(String path) {
        String[] parts = path.split("/");
        if (parts.length > 1) {
            return Long.parseLong(parts[1]);
        }
        return 1L;
    }
}
