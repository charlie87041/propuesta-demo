package com.cookiesstore.admin.web.interceptos;

import com.cookiesstore.admin.web.controllers.AdminUserViewController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

@ControllerAdvice(assignableTypes = AdminUserViewController.class, annotations = Controller.class)
public class AdminUserViewExceptionAdvice {

    @ExceptionHandler(RuntimeException.class)
    public ModelAndView handleRuntime(
        RuntimeException ex,
        HttpServletRequest request,
        HttpServletResponse response,
        Model model
    ) {
        String path = request.getRequestURI();
        if (path != null && path.endsWith("/deactivate")) {
            FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
            flashMap.put("errorMessage", ex.getMessage());
            RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
            return new ModelAndView("redirect:/admin/users");
        }

        if ("GET".equalsIgnoreCase(request.getMethod())) {
            FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
            flashMap.put("errorMessage", ex.getMessage());
            RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
            return new ModelAndView("redirect:/admin/users");
        }

        model.addAttribute("errorMessage", ex.getMessage());
        return new ModelAndView("backoffice/users/form");
    }
}
