package com.cookiesstore.admin.web.advice.exception;

import com.cookiesstore.admin.service.categories.CategoryDomainException;
import com.cookiesstore.admin.service.categories.CategoryNotFoundException;
import com.cookiesstore.admin.web.controllers.CategoriesController;
import com.cookiesstore.admin.web.dto.categories.CreateCategoryForm;
import com.cookiesstore.admin.web.dto.categories.UpdateCategoryForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;
import com.cookiesstore.common.repositories.ProductTemplateRepository;
import org.springframework.data.domain.Sort;

@ControllerAdvice(assignableTypes = CategoriesController.class)
public class CategoriesViewExceptionAdvice {

    private static final Pattern CATEGORY_EDIT_PATH_PATTERN = Pattern.compile("^/admin/categories/(\\d+)$");

    private final MessageSource messageSource;
    private final ProductTemplateRepository productTemplateRepository;

    public CategoriesViewExceptionAdvice(
        MessageSource messageSource,
        ProductTemplateRepository productTemplateRepository
    ) {
        this.messageSource = messageSource;
        this.productTemplateRepository = productTemplateRepository;
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
            String path = request.getRequestURI();
            Matcher matcher = CATEGORY_EDIT_PATH_PATTERN.matcher(path != null ? path : "");
            boolean isEdit = matcher.matches();

            model.addAttribute("errorMessage", message(ex.getMessageKey()));
            model.addAttribute("isEdit", isEdit);
            model.addAttribute("pageTitle", message(isEdit ? "admin.categories.edit.title" : "admin.categories.create.title"));
            model.addAttribute("formAction", isEdit ? path : "/admin/categories");
            model.addAttribute("submitLabel", message(isEdit ? "admin.categories.submit.edit" : "admin.categories.submit.create"));
            model.addAttribute("productTemplates", productTemplateRepository.findByLatestTrue(Sort.by(Sort.Order.asc("name"))));
            model.addAttribute("form", buildFormFromRequest(request, isEdit));
            return new ModelAndView("backoffice/categories/form");
        }
        return new ModelAndView("redirect:/admin/categories");
    }

    private Object buildFormFromRequest(HttpServletRequest request, boolean isEdit) {
        String code = nullToEmpty(request.getParameter("code"));
        String name = nullToEmpty(request.getParameter("name"));
        String slug = nullToEmpty(request.getParameter("slug"));
        String description = request.getParameter("description");
        int sortOrder = parseIntSafely(request.getParameter("sortOrder"));
        boolean active = Boolean.parseBoolean(request.getParameter("active"));
        Long productTemplateId = parseLongSafely(request.getParameter("productTemplateId"));
        if (isEdit) {
            return new UpdateCategoryForm(code, name, slug, description, sortOrder, active, productTemplateId);
        }
        return new CreateCategoryForm(code, name, slug, description, sortOrder, active, productTemplateId);
    }

    private static String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    private static int parseIntSafely(String value) {
        if (value == null) return 0;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static Long parseLongSafely(String value) {
        if (value == null) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

}
