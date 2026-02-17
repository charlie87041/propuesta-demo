package com.cookiesstore.common.authorization.evaluator;

import com.cookiesstore.common.authorization.service.DomainAuthorizationService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("domainAuthorizationEvaluator")
public class DomainAuthorizationEvaluator {

    private final DomainAuthorizationService authorizationService;

    public DomainAuthorizationEvaluator(DomainAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    public boolean hasPermission(Authentication authentication, String domainCode, String permissionCode) {
        Long userId = extractUserId(authentication);
        if (userId == null) {
            return false;
        }
        return authorizationService.hasPermission(userId, domainCode, permissionCode);
    }

    public boolean hasAbility(Authentication authentication, String domainCode, String abilityCode) {
        Long userId = extractUserId(authentication);
        if (userId == null) {
            return false;
        }
        return authorizationService.hasAbility(userId, domainCode, abilityCode);
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Long userId) {
            return userId;
        }
        if (principal instanceof String textPrincipal) {
            try {
                return Long.parseLong(textPrincipal);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        if (principal instanceof UserDetails userDetails) {
            try {
                return Long.parseLong(userDetails.getUsername());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }
}
