package com.cookiesstore.pos.web.advice.exception;

import com.cookiesstore.pos.services.PosOrderDomainException;
import com.cookiesstore.pos.web.PosOrderController;
import com.cookiesstore.pos.web.advice.support.BaseAdviceSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

@ControllerAdvice(assignableTypes = PosOrderController.class)
public class PosOrderViewExceptionAdvice extends BaseAdviceSupport {

    private final MessageSource messageSource;

    public PosOrderViewExceptionAdvice(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(PosOrderDomainException.class)
    public ModelAndView handleDomain(
        PosOrderDomainException ex,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        return redirectWithError(message(ex.getMessageKey()), request, response);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnexpected(
        Exception ex,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        return redirectWithError(message("pos.order.create.error"), request, response);
    }

    private ModelAndView redirectWithError(String errorMessage, HttpServletRequest request, HttpServletResponse response) {
        FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
        flashMap.put("errorMessage", errorMessage);
        RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
        return new ModelAndView("redirect:" + resolveRedirectPath(request));
    }

    private String resolveRedirectPath(HttpServletRequest request) {
        String requestUri = request == null ? null : request.getRequestURI();
        Long sourceId = extractSourceId(request == null ? null : request.getRequestURI());
        if (sourceId == null) {
            return "/pos/login";
        }
        if (requestUri != null && requestUri.matches("^/\\d+/pos/orders/\\d+/(pay|cancel|resume|resume/available|resume/cancel)$")) {
            return "/" + sourceId + "/pos/order-history";
        }
        return "/" + sourceId + "/pos/terminal";
    }

    private Long extractSourceId(String path) {
        if (path == null) {
            return null;
        }
        String[] segments = path.split("/");
        if (segments.length < 2) {
            return null;
        }
        try {
            return Long.parseLong(segments[1]);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, key, LocaleContextHolder.getLocale());
    }
}
