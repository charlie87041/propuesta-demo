package com.cookiesstore.admin.web.advice.exception;

import com.cookiesstore.admin.service.sources.SourceNotFoundException;
import com.cookiesstore.admin.web.controllers.SourceStockMovementController;
import com.cookiesstore.common.services.stock.StockMovementDomainException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

@ControllerAdvice(assignableTypes = SourceStockMovementController.class)
public class SourceStockMovementViewExceptionAdvice {

    private final MessageSource messageSource;

    public SourceStockMovementViewExceptionAdvice(MessageSource messageSource) {
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

    @ExceptionHandler(StockMovementDomainException.class)
    public ModelAndView handleDomain(
        StockMovementDomainException ex,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
        flashMap.put("errorMessage", ex.getMessage());
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
        Long sourceId = extractSourceId(request == null ? null : request.getRequestURI());
        if (sourceId == null) {
            return "/admin/product-sources";
        }
        return "/admin/product-sources/" + sourceId + "/manage/movements";
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
