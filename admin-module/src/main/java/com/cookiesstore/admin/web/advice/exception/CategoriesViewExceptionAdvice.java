package com.cookiesstore.admin.web.advice.exception;

import com.cookiesstore.admin.service.categories.CategoryDomainException;
import com.cookiesstore.admin.service.categories.CategoryNotFoundException;
import com.cookiesstore.admin.web.controllers.CategoriesController;
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

@ControllerAdvice(assignableTypes = CategoriesController.class)
public class CategoriesViewExceptionAdvice {

    private final MessageSource messageSource;

    public CategoriesViewExceptionAdvice(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ModelAndView handleNotFound(
        CategoryNotFoundException ex,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
        flashMap.put("errorMessage", message(ex.getMessageKey()));
        RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
        return new ModelAndView("redirect:/admin/categories");
    }

    @ExceptionHandler(CategoryDomainException.class)
    public ModelAndView handleDomain(
        CategoryDomainException ex,
        HttpServletRequest request,
        Model model
    ) {
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            model.addAttribute("errorMessage", message(ex.getMessageKey()));
            return new ModelAndView("backoffice/categories/form");
        }
        return new ModelAndView("redirect:/admin/categories");
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
