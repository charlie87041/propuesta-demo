package com.cookiesstore.admin.web.advice.model;

import com.cookiesstore.admin.web.advice.support.BaseAdviceSupport;
import com.cookiesstore.admin.web.controllers.ProductsController;
import com.cookiesstore.common.repositories.CategoryRepository;
import com.cookiesstore.common.repositories.PackageOptionTypeRepository;
import com.cookiesstore.common.repositories.ProductRepository;
import com.cookiesstore.common.repositories.ProductTemplateFieldRepository;
import com.cookiesstore.common.repositories.SourceRepository;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;

@ControllerAdvice(assignableTypes = ProductsController.class)
public class ProductsModelAdvice extends BaseAdviceSupport {

    private final CategoryRepository categoryRepository;
    private final SourceRepository sourceRepository;
    private final ProductRepository productRepository;
    private final PackageOptionTypeRepository packageOptionTypeRepository;
    private final ProductTemplateFieldRepository productTemplateFieldRepository;

    public ProductsModelAdvice(
        CategoryRepository categoryRepository,
        SourceRepository sourceRepository,
        ProductRepository productRepository,
        PackageOptionTypeRepository packageOptionTypeRepository,
        ProductTemplateFieldRepository productTemplateFieldRepository,
        MessageSource messageSource
    ) {
        super(messageSource);
        this.categoryRepository = categoryRepository;
        this.sourceRepository = sourceRepository;
        this.productRepository = productRepository;
        this.packageOptionTypeRepository = packageOptionTypeRepository;
        this.productTemplateFieldRepository = productTemplateFieldRepository;
    }

    @ModelAttribute
    public void populateProductsViewModel(
        Model model,
        @PathVariable(value = "productId", required = false) Long productId,
        @RequestParam(value = "sort", required = false) List<String> sortParams,
        @RequestParam(value = "q", required = false) String searchQuery,
        NativeWebRequest webRequest
    ) {
        Long actorUserId = currentUserId();
        model.addAttribute("currentUserId", actorUserId);

        String routeName = resolveRouteName(webRequest);
        if ("admin.products.list".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.products.title"));
            model.addAttribute("activeNav", "products");
            model.addAttribute("sortParams", sortParams);
            model.addAttribute("searchQuery", searchQuery);
            return;
        }

        if ("admin.products.create.view".equals(routeName) || "admin.products.create".equals(routeName)) {
            var categories = categoryRepository.findAll(Sort.by(Sort.Order.asc("sortOrder"), Sort.Order.asc("name")));
            model.addAttribute("pageTitle", message("admin.products.create.title"));
            model.addAttribute("activeNav", "products");
            model.addAttribute("isEdit", false);
            model.addAttribute("formAction", "/admin/products");
            model.addAttribute("submitLabel", message("admin.products.submit.create"));
            model.addAttribute("categories", categories);
            model.addAttribute("categoryTemplateMap", buildCategoryTemplateMap(categories));
            model.addAttribute("sources", sourceRepository.findAll(Sort.by(Sort.Order.asc("name"))));
            model.addAttribute("bundleComponentProducts", productRepository.findAll(Sort.by(Sort.Order.asc("name"))));
            model.addAttribute("packageOptionTypes", packageOptionTypeRepository.findAll(Sort.by(Sort.Order.asc("name"))));
            model.addAttribute("supportedProductTypes", List.of("SIMPLE", "BUNDLE", "PACKAGE", "ADD_ON"));
            model.addAttribute("templateValuesFromRequest", parseTemplateValues(webRequest.getParameterMap()));
            return;
        }

        if ("admin.products.edit.view".equals(routeName) || "admin.products.update".equals(routeName)) {
            var categories = categoryRepository.findAll(Sort.by(Sort.Order.asc("sortOrder"), Sort.Order.asc("name")));
            model.addAttribute("pageTitle", message("admin.products.edit.title"));
            model.addAttribute("activeNav", "products");
            model.addAttribute("isEdit", true);
            model.addAttribute("productId", productId);
            model.addAttribute("formAction", "/admin/products/" + productId);
            model.addAttribute("submitLabel", message("admin.products.submit.edit"));
            model.addAttribute("categories", categories);
            model.addAttribute("categoryTemplateMap", buildCategoryTemplateMap(categories));
            model.addAttribute("sources", sourceRepository.findAll(Sort.by(Sort.Order.asc("name"))));
            model.addAttribute("bundleComponentProducts", productRepository.findAll(Sort.by(Sort.Order.asc("name"))));
            model.addAttribute("packageOptionTypes", packageOptionTypeRepository.findAll(Sort.by(Sort.Order.asc("name"))));
            model.addAttribute("supportedProductTypes", List.of("SIMPLE", "BUNDLE", "PACKAGE", "ADD_ON"));
            model.addAttribute("templateValuesFromRequest", parseTemplateValues(webRequest.getParameterMap()));
            return;
        }

        if ("admin.products.show".equals(routeName)) {
            model.addAttribute("pageTitle", message("admin.products.show.title"));
            model.addAttribute("activeNav", "products");
            model.addAttribute("productId", productId);
        }
    }

    private Map<Long, Map<String, Object>> buildCategoryTemplateMap(List<com.cookiesstore.common.entities.Category> categories) {
        Map<Long, Map<String, Object>> categoryTemplateMap = new LinkedHashMap<>();
        for (com.cookiesstore.common.entities.Category category : categories) {
            if (category.getDefaultTemplate() == null || category.getDefaultTemplate().getId() == null) {
                continue;
            }
            var template = category.getDefaultTemplate();
            var templateFields = productTemplateFieldRepository.findByTemplateIdOrderBySortOrderAsc(template.getId())
                .stream()
                .map(field -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("id", field.getId());
                    payload.put("fieldKey", field.getFieldKey());
                    payload.put("label", field.getLabel());
                    payload.put("fieldType", field.getFieldType().name());
                    payload.put("required", field.isRequired());
                    payload.put("defaultValue", field.getDefaultValue());
                    payload.put("validationRules", field.getValidationRules());
                    payload.put("sortOrder", field.getSortOrder());
                    return payload;
                })
                .toList();

            Map<String, Object> templatePayload = new LinkedHashMap<>();
            templatePayload.put("templateId", template.getId());
            templatePayload.put("templateCode", template.getCode());
            templatePayload.put("templateName", template.getName());
            templatePayload.put("fields", templateFields);

            categoryTemplateMap.put(category.getId(), templatePayload);
        }
        return categoryTemplateMap;
    }

    private Map<String, String> parseTemplateValues(Map<String, String[]> parameterMap) {
        Map<String, String> values = new HashMap<>();
        parameterMap.forEach((key, rawValue) -> {
            String mapKey = parseStringIndexedKey(key, "templateValues");
            if (mapKey == null || rawValue == null || rawValue.length == 0) {
                return;
            }
            values.put(mapKey, rawValue[0]);
        });
        return values;
    }

    private String parseStringIndexedKey(String rawKey, String prefix) {
        if (rawKey == null || !rawKey.startsWith(prefix + "[") || !rawKey.endsWith("]")) {
            return null;
        }
        String value = rawKey.substring(prefix.length() + 1, rawKey.length() - 1);
        return value.isBlank() ? null : value;
    }
}
