package com.cookiesstore.admin.web.advice.exception;

import com.cookiesstore.admin.web.controllers.CurrenciesController;
import com.cookiesstore.admin.service.settings.currencies.CurrencyDomainException;
import com.cookiesstore.admin.service.settings.currencies.CurrencyNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

@ControllerAdvice(assignableTypes = CurrenciesController.class)
public class CurrenciesViewExceptionAdvice {

    private final MessageSource messageSource;

    public CurrenciesViewExceptionAdvice(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(CurrencyNotFoundException.class)
    public ModelAndView handleNotFound(
        CurrencyNotFoundException ex,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
        flashMap.put("errorMessage", message(ex.getMessageKey()));
        RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
        return new ModelAndView("redirect:/admin/settings/currencies");
    }

    @ExceptionHandler(CurrencyDomainException.class)
    public ModelAndView handleDomain(
        CurrencyDomainException ex,
        HttpServletRequest request,
        HttpServletResponse response,
        Model model
    ) {
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String path = request.getRequestURI();
            if (path != null && path.endsWith("/delete")) {
                FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
                flashMap.put("errorMessage", message(ex.getMessageKey()));
                RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
                return new ModelAndView("redirect:/admin/settings/currencies");
            }
            model.addAttribute("errorMessage", message(ex.getMessageKey()));
            return new ModelAndView("backoffice/settings/currencies/form");
        }

        FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
        flashMap.put("errorMessage", message(ex.getMessageKey()));
        RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
        return new ModelAndView("redirect:/admin/settings/currencies");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ModelAndView handleIntegrityViolation(
        DataIntegrityViolationException ex,
        HttpServletRequest request,
        HttpServletResponse response,
        Model model
    ) {
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            model.addAttribute("errorMessage", message("admin.settings.currencies.error.integrity"));
            return new ModelAndView("backoffice/settings/currencies/form");
        }

        FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
        flashMap.put("errorMessage", message("admin.settings.currencies.error.integrity"));
        RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
        return new ModelAndView("redirect:/admin/settings/currencies");
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnexpected(
        Exception ex,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
        flashMap.put("errorMessage", message("admin.settings.currencies.error.unexpected"));
        RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
        return new ModelAndView("redirect:/admin/settings/currencies");
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
