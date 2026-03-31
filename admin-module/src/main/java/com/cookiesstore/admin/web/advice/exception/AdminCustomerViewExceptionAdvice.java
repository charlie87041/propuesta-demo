package com.cookiesstore.admin.web.advice.exception;

import com.cookiesstore.admin.web.controllers.AdminCustomerViewController;
import com.cookiesstore.common.services.customers.CustomerDomainException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

@ControllerAdvice(assignableTypes = AdminCustomerViewController.class)
public class AdminCustomerViewExceptionAdvice {

    private final MessageSource messageSource;

    public AdminCustomerViewExceptionAdvice(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(CustomerDomainException.class)
    public ModelAndView handleDomain(
        CustomerDomainException ex,
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
            return new ModelAndView("redirect:/admin/customers");
        }

        if (path != null && (path.endsWith("/deactivate") || path.endsWith("/enable"))) {
            FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
            flashMap.put("errorMessage", localizedMessage);
            RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
            return new ModelAndView("redirect:/admin/customers");
        }

        model.addAttribute("errorMessage", localizedMessage);
        return new ModelAndView("backoffice/customers/form");
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
