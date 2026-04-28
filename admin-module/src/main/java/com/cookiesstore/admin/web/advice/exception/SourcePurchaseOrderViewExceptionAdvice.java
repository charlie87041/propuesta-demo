package com.cookiesstore.admin.web.advice.exception;

import com.cookiesstore.admin.service.products.ProductDomainException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.cookiesstore.admin.service.sources.SourceDomainException;
import com.cookiesstore.admin.service.sources.SourceNotFoundException;
import com.cookiesstore.admin.web.controllers.SourcePurchaseOrderController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ControllerAdvice(assignableTypes = SourcePurchaseOrderController.class)
public class SourcePurchaseOrderViewExceptionAdvice {

    private final MessageSource messageSource;

    public SourcePurchaseOrderViewExceptionAdvice(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(SourceNotFoundException.class)
    public ModelAndView handleSourceNotFound(
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
    public ModelAndView handleSourceDomainException(
        SourceDomainException ex,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
        flashMap.put("errorMessage", message(ex.getMessageKey()));
        RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
        return new ModelAndView("redirect:" + resolveRedirectPath(request));
    }

    @ExceptionHandler(ProductDomainException.class)
    public ModelAndView handleProductDomainException(
        ProductDomainException ex,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
        flashMap.put("errorMessage", message(ex.getMessageKey()));
        RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
        return new ModelAndView("redirect:" + resolveRedirectPath(request));
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnexpected(
        Exception ex,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
        flashMap.put("errorMessage", message("admin.error.500.body"));
        RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
        return new ModelAndView("redirect:" + resolveRedirectPath(request));
    }

    private String resolveRedirectPath(HttpServletRequest request) {
        String requestUri = request == null ? null : request.getRequestURI();
        if (requestUri != null && requestUri.endsWith("/resolve-adhoc")) {
            return requestUri;
        }
        Long sourceId = extractSourceId(requestUri);
        if (sourceId == null) {
            return "/admin/product-sources";
        }
        return "/admin/product-sources/" + sourceId + "/manage/purchase-orders";
    }

    private Long extractSourceId(String path) {
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
