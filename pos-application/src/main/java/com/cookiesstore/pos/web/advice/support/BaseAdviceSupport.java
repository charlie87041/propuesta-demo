package com.cookiesstore.pos.web.advice.support;

import com.cookiesstore.pos.auth.PosPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class BaseAdviceSupport {

    protected PosPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AccessDeniedException("Unauthenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof PosPrincipal posPrincipal) {
            return posPrincipal;
        }

        throw new AccessDeniedException("Invalid authentication principal");
    }

    protected Long currentUserId() {
        return currentPrincipal().userId();
    }
}
