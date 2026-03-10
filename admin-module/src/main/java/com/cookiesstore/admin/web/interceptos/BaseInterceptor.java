package com.cookiesstore.admin.web.interceptos;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

public class BaseInterceptor {


    protected final MessageSource messageSource;

    public BaseInterceptor(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    
    protected Long currentUserId() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
            .getContext()
            .getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AccessDeniedException("Unauthenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Long userId) {
            return userId;
        }
        if (principal instanceof String textPrincipal) {
            return Long.parseLong(textPrincipal);
        }

        throw new AccessDeniedException("Invalid authentication principal");
    }


    protected String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    protected String resolveRouteName(NativeWebRequest webRequest) {
        Object handler = webRequest.getAttribute(
            HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE,
            NativeWebRequest.SCOPE_REQUEST
        );
        if (handler instanceof HandlerMethod handlerMethod) {
            org.springframework.web.bind.annotation.RequestMapping mapping =
                org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), org.springframework.web.bind.annotation.RequestMapping.class);
            if (mapping != null && StringUtils.hasText(mapping.name())) {
                return mapping.name();
            }
        }
        return null;
    }

}
