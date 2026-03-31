package com.cookiesstore.admin.web.advice.exception;

import com.cookiesstore.admin.service.sources.SourceDomainException;
import com.cookiesstore.admin.service.sources.SourceNotFoundException;
import com.cookiesstore.admin.web.controllers.ProductSourcesController;
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

@ControllerAdvice(assignableTypes = ProductSourcesController.class)
public class ProductSourcesViewExceptionAdvice {

    private final MessageSource messageSource;

    public ProductSourcesViewExceptionAdvice(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(SourceNotFoundException.class)
    public ModelAndView handleNotFound(
        SourceNotFoundException ex,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
        flashMap.put("errorMessage", message(ex.getMessageKey()));
        RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
        return new ModelAndView("redirect:/admin/product-sources");
    }

    @ExceptionHandler(SourceDomainException.class)
    public ModelAndView handleDomain(
        SourceDomainException ex,
        HttpServletRequest request,
        HttpServletResponse response,
        Model model
    ) {
        String path = request.getRequestURI();
        if ("POST".equalsIgnoreCase(request.getMethod()) && path != null && path.endsWith("/delete")) {
            FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
            flashMap.put("errorMessage", message(ex.getMessageKey()));
            RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
            return new ModelAndView("redirect:/admin/product-sources");
        }

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            model.addAttribute("errorMessage", message(ex.getMessageKey()));
            return new ModelAndView("backoffice/product-sources/form");
        }
        return new ModelAndView("redirect:/admin/product-sources");
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
