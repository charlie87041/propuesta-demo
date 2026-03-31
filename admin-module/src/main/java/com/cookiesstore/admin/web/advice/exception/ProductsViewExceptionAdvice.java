package com.cookiesstore.admin.web.advice.exception;

import com.cookiesstore.admin.service.products.ProductDomainException;
import com.cookiesstore.admin.service.products.ProductNotFoundException;
import com.cookiesstore.admin.service.sources.SourceNotFoundException;
import com.cookiesstore.admin.web.controllers.ProductsController;
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

@ControllerAdvice(assignableTypes = ProductsController.class)
public class ProductsViewExceptionAdvice {

    private final MessageSource messageSource;

    public ProductsViewExceptionAdvice(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ModelAndView handleNotFound(
        ProductNotFoundException ex,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
        flashMap.put("errorMessage", message(ex.getMessageKey()));
        RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
        return new ModelAndView("redirect:/admin/products");
    }

    @ExceptionHandler(ProductDomainException.class)
    public ModelAndView handleDomain(
        ProductDomainException ex,
        HttpServletRequest request,
        HttpServletResponse response,
        Model model
    ) {
        String path = request.getRequestURI();
        if ("POST".equalsIgnoreCase(request.getMethod())
            && path != null
            && (path.endsWith("/delete") || path.endsWith("/deactivate") || path.endsWith("/enable"))) {
            FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
            flashMap.put("errorMessage", message(ex.getMessageKey()));
            RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
            return new ModelAndView("redirect:/admin/products");
        }

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            model.addAttribute("errorMessage", message(ex.getMessageKey()));
            return new ModelAndView("backoffice/products/form");
        }

        return new ModelAndView("redirect:/admin/products");
    }

    @ExceptionHandler(SourceNotFoundException.class)
    public ModelAndView handleSource(
        SourceNotFoundException ex,
        HttpServletRequest request,
        HttpServletResponse response,
        Model model
    ) {
        String path = request.getRequestURI();
        if ("POST".equalsIgnoreCase(request.getMethod())
            && path != null
            && (path.endsWith("/delete") || path.endsWith("/deactivate") || path.endsWith("/enable"))) {
            FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
            flashMap.put("errorMessage", message(ex.getMessageKey()));
            RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
            return new ModelAndView("redirect:/admin/products");
        }
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            model.addAttribute("errorMessage", message(ex.getMessageKey()));
            return new ModelAndView("backoffice/products/form");
        }
        return new ModelAndView("redirect:/admin/products");
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
