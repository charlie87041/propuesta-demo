package com.cookiesstore.admin.web.advice.exception;

import com.cookiesstore.admin.service.users.AdminUserDomainException;
import com.cookiesstore.admin.web.controllers.AdminUserViewController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerAdvice(assignableTypes = AdminUserViewController.class)
public class AdminUserViewExceptionAdvice {

    private static final Logger log = LoggerFactory.getLogger(AdminUserViewExceptionAdvice.class);
    private final MessageSource messageSource;

    public AdminUserViewExceptionAdvice(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler({AccessDeniedException.class, AuthenticationException.class})
    public ModelAndView handleAuthentication(
        RuntimeException ex,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        log.warn("Admin auth exception on {} {} (status: {}): {}",
            request.getMethod(),
            request.getRequestURI(),
            response.getStatus(),
            ex.getMessage()
        );
        return new ModelAndView("redirect:/admin/login");
    }

    @ExceptionHandler(RuntimeException.class)
    public ModelAndView handleRuntime(
        RuntimeException ex,
        HttpServletRequest request,
        HttpServletResponse response,
        Model model
    ) {
        String path = request.getRequestURI();
        log.error("Admin view runtime exception on {} {} (status: {}): {}",
            request.getMethod(),
            path,
            response.getStatus(),
            ex.getMessage(),
            ex
        );
        if (path != null && path.endsWith("/deactivate")) {
            FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
            flashMap.put("errorMessage", ex.getMessage());
            RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
            return new ModelAndView("redirect:/admin/users");
        }

        if ("GET".equalsIgnoreCase(request.getMethod())) {
            if (path != null && path.startsWith("/admin/users")) {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                model.addAttribute("errorMessage", ex.getMessage());
                return new ModelAndView("error/500");
            }
            FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
            flashMap.put("errorMessage", ex.getMessage());
            RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
            return new ModelAndView("redirect:/admin/users");
        }

        model.addAttribute("errorMessage", ex.getMessage());
        return new ModelAndView("backoffice/users/form");
    }

    @ExceptionHandler(AdminUserDomainException.class)
    public ModelAndView handleDomain(
        AdminUserDomainException ex,
        HttpServletRequest request,
        HttpServletResponse response,
        Model model
    ) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        String localizedMessage = message(ex.getMessageKey());

        if ("GET".equalsIgnoreCase(method)) {
            FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
            flashMap.put("errorMessage", localizedMessage);
            RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
            return new ModelAndView("redirect:/admin/users");
        }

        if (path != null && (path.endsWith("/deactivate") || path.endsWith("/enable"))) {
            FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
            flashMap.put("errorMessage", localizedMessage);
            RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
            return new ModelAndView("redirect:/admin/users");
        }

        model.addAttribute("errorMessage", localizedMessage);
        return new ModelAndView("backoffice/users/form");
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
