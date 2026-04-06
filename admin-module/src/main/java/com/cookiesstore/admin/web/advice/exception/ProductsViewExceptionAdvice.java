package com.cookiesstore.admin.web.advice.exception;

import com.cookiesstore.admin.service.products.ProductDomainException;
import com.cookiesstore.admin.service.products.ProductNotFoundException;
import com.cookiesstore.admin.service.products.PackageOptionValidationException;
import com.cookiesstore.admin.service.sources.SourceNotFoundException;
import com.cookiesstore.admin.web.dto.products.CreateProductForm;
import com.cookiesstore.admin.web.dto.products.UpdateProductForm;
import com.cookiesstore.admin.web.controllers.ProductsController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
        HttpServletResponse response
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
            FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
            flashMap.put("errorMessage", message(ex.getMessageKey()));
            flashMap.put("form", buildFormFromRequest(request, path));
            RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
            return new ModelAndView("redirect:" + resolveFormRedirect(path));
        }

        return new ModelAndView("redirect:/admin/products");
    }

    @ExceptionHandler(PackageOptionValidationException.class)
    public ModelAndView handlePackageOptionValidation(
        PackageOptionValidationException ex,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        return handleDomain(ex, request, response);
    }

    @ExceptionHandler(SourceNotFoundException.class)
    public ModelAndView handleSource(
        SourceNotFoundException ex,
        HttpServletRequest request,
        HttpServletResponse response
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
            FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
            flashMap.put("errorMessage", message(ex.getMessageKey()));
            flashMap.put("form", buildFormFromRequest(request, path));
            RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
            return new ModelAndView("redirect:" + resolveFormRedirect(path));
        }
        return new ModelAndView("redirect:/admin/products");
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    private String resolveFormRedirect(String path) {
        if (path == null) {
            return "/admin/products";
        }
        if ("/admin/products".equals(path)) {
            return "/admin/products/new";
        }
        if (path.matches("^/admin/products/\\d+$")) {
            return path + "/edit";
        }
        return "/admin/products";
    }

    private Object buildFormFromRequest(HttpServletRequest request, String path) {
        List<Long> sourceIds = parseLongList(request.getParameterValues("sourceIds"));
        Map<Long, Double> sourcePrices = parseDoubleMap(request.getParameterMap(), "sourcePrices");
        Map<Long, Integer> sourceStockQuantities = parseIntegerMap(request.getParameterMap(), "sourceStockQuantities");
        Map<Long, Integer> sourceLowStockThresholds = parseIntegerMap(request.getParameterMap(), "sourceLowStockThresholds");
        Map<String, String> templateValues = parseStringMap(request.getParameterMap(), "templateValues");

        String sku = value(request, "sku");
        String name = value(request, "name");
        String slug = value(request, "slug");
        String description = value(request, "description");
        Long categoryId = parseLong(value(request, "categoryId"));
        String mainImageUrl = value(request, "mainImageUrl");
        Integer stockQuantity = parseInteger(value(request, "stockQuantity"));
        Integer lowStockThreshold = parseInteger(value(request, "lowStockThreshold"));
        Double price = parseDouble(value(request, "price"));
        String productTypeCode = value(request, "productTypeCode");
        boolean active = parseBoolean(value(request, "active"));
        boolean visible = parseBoolean(value(request, "visible"));
        boolean isListable = parseBooleanOrDefault(value(request, "isListable"), true);
        boolean isPurchasable = parseBooleanOrDefault(value(request, "isPurchasable"), true);
        boolean isPurchasableAlone = parseBooleanOrDefault(value(request, "isPurchasableAlone"), true);

        if ("/admin/products".equals(path)) {
            return new CreateProductForm(
                sku,
                name,
                slug,
                description,
                categoryId,
                mainImageUrl,
                active,
                visible,
                sourceIds,
                stockQuantity,
                lowStockThreshold,
                price,
                productTypeCode,
                null,
                null,
                sourcePrices,
                sourceStockQuantities,
                sourceLowStockThresholds,
                templateValues,
                isListable,
                isPurchasable,
                isPurchasableAlone,
                null,
                null
            );
        }

        return new UpdateProductForm(
            sku,
            name,
            slug,
            description,
            categoryId,
            mainImageUrl,
            sourceIds,
            stockQuantity,
            lowStockThreshold,
            price,
            productTypeCode,
            null,
            null,
            sourcePrices,
            sourceStockQuantities,
            sourceLowStockThresholds,
            templateValues,
            active,
            visible,
            isListable,
            isPurchasable,
            isPurchasableAlone,
            null,
            List.of()
        );
    }

    private String value(HttpServletRequest request, String key) {
        String value = request.getParameter(key);
        return value == null ? "" : value;
    }

    private List<Long> parseLongList(String[] values) {
        if (values == null) {
            return List.of();
        }
        return java.util.Arrays.stream(values)
            .map(this::parseLong)
            .filter(java.util.Objects::nonNull)
            .toList();
    }

    private Map<Long, Double> parseDoubleMap(Map<String, String[]> parameterMap, String prefix) {
        Map<Long, Double> values = new HashMap<>();
        parameterMap.forEach((key, rawValue) -> {
            Long sourceId = parseIndexedKey(key, prefix);
            if (sourceId == null || rawValue == null || rawValue.length == 0) {
                return;
            }
            Double value = parseDouble(rawValue[0]);
            if (value != null) {
                values.put(sourceId, value);
            }
        });
        return values;
    }

    private Map<Long, Integer> parseIntegerMap(Map<String, String[]> parameterMap, String prefix) {
        Map<Long, Integer> values = new HashMap<>();
        parameterMap.forEach((key, rawValue) -> {
            Long sourceId = parseIndexedKey(key, prefix);
            if (sourceId == null || rawValue == null || rawValue.length == 0) {
                return;
            }
            Integer value = parseInteger(rawValue[0]);
            if (value != null) {
                values.put(sourceId, value);
            }
        });
        return values;
    }

    private Map<String, String> parseStringMap(Map<String, String[]> parameterMap, String prefix) {
        Map<String, String> values = new HashMap<>();
        parameterMap.forEach((key, rawValue) -> {
            String mapKey = parseStringIndexedKey(key, prefix);
            if (mapKey == null || rawValue == null || rawValue.length == 0) {
                return;
            }
            values.put(mapKey, rawValue[0]);
        });
        return values;
    }

    private Long parseIndexedKey(String key, String prefix) {
        String start = prefix + "[";
        if (!key.startsWith(start) || !key.endsWith("]")) {
            return null;
        }
        String token = key.substring(start.length(), key.length() - 1);
        return parseLong(token);
    }

    private String parseStringIndexedKey(String key, String prefix) {
        String start = prefix + "[";
        if (!key.startsWith(start) || !key.endsWith("]")) {
            return null;
        }
        return key.substring(start.length(), key.length() - 1);
    }

    private Long parseLong(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer parseInteger(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Double parseDouble(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean parseBoolean(String raw) {
        return "true".equalsIgnoreCase(raw) || "on".equalsIgnoreCase(raw) || "1".equals(raw);
    }

    private boolean parseBooleanOrDefault(String raw, boolean defaultValue) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        return parseBoolean(raw);
    }
}
