package com.cookiesstore.admin.web.advice.exception;

import com.cookiesstore.admin.service.customers.CustomerAddressDomainException;
import com.cookiesstore.admin.web.controllers.CustomerAddressController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

@ControllerAdvice(assignableTypes = CustomerAddressController.class)
public class CustomerAddressViewExceptionAdvice {

    private final MessageSource messageSource;

    public CustomerAddressViewExceptionAdvice(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(CustomerAddressDomainException.class)
    public ModelAndView handleDomain(
        CustomerAddressDomainException ex,
        HttpServletRequest request,
        HttpServletResponse response,
        Model model
    ) {
        Long customerId = extractCustomerId(request.getRequestURI());
        if (customerId == null) {
            return new ModelAndView("redirect:/admin/customers");
        }

        FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
        flashMap.put("errorMessage", message(ex.getMessageKey()));
        RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
        return new ModelAndView("redirect:/admin/customers/" + customerId + "/edit#customer-addresses");
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnexpected(
        Exception ex,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        Long customerId = extractCustomerId(request.getRequestURI());
        if (customerId == null) {
            return new ModelAndView("redirect:/admin/customers");
        }
        FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
        flashMap.put("errorMessage", message("admin.customers.addresses.error.unexpected"));
        RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
        return new ModelAndView("redirect:/admin/customers/" + customerId + "/edit#customer-addresses");
    }

    private Long extractCustomerId(String path) {
        if (path == null) {
            return null;
        }
        String[] segments = path.split("/");
        if (segments.length < 4) {
            return null;
        }
        try {
            return Long.parseLong(segments[3]);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
