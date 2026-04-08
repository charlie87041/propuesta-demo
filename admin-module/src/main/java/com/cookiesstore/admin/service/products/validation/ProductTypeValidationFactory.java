package com.cookiesstore.admin.service.products.validation;

import com.cookiesstore.admin.service.products.VariantProductService;
import com.cookiesstore.admin.service.products.SimpleProductService;
import com.cookiesstore.admin.web.dto.products.CreateProductForm;
import com.cookiesstore.admin.web.dto.products.UpdateProductForm;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ProductTypeValidationFactory {

    private final Map<String, ProductTypeValidationStrategy> strategiesByType;
    private final ProductTypeValidationStrategy commonStrategy;
    private final ProductTypeValidationStrategy defaultStrategy;

    public ProductTypeValidationFactory(List<ProductTypeValidationStrategy> strategies) {
        this.strategiesByType = strategies.stream()
            .collect(Collectors.toMap(ProductTypeValidationStrategy::supportedTypeCode, Function.identity()));
        this.commonStrategy = resolveCommon(strategiesByType);
        this.defaultStrategy = resolveDefault(strategiesByType);
    }

    public void validateCreate(String typeCode, CreateProductForm form) {
        commonStrategy.validateCreate(form);
        ProductTypeValidationStrategy specific = resolve(typeCode);
        if (!specific.supportedTypeCode().equals(commonStrategy.supportedTypeCode())) {
            specific.validateCreate(form);
        }
    }

    public void validateUpdate(String typeCode, Long productId, UpdateProductForm form) {
        commonStrategy.validateUpdate(productId, form);
        ProductTypeValidationStrategy specific = resolve(typeCode);
        if (!specific.supportedTypeCode().equals(commonStrategy.supportedTypeCode())) {
            specific.validateUpdate(productId, form);
        }
    }

    private ProductTypeValidationStrategy resolve(String typeCode) {
        if (typeCode != null) {
            ProductTypeValidationStrategy strategy = strategiesByType.get(typeCode);
            if (strategy != null) {
                return strategy;
            }
            if (VariantProductService.PARENT_TYPE_CODE.equals(typeCode)) {
                ProductTypeValidationStrategy variantStrategy = strategiesByType.get(VariantProductService.TYPE_CODE);
                if (variantStrategy != null) {
                    return variantStrategy;
                }
            }
        }
        return defaultStrategy;
    }

    private ProductTypeValidationStrategy resolveDefault(Map<String, ProductTypeValidationStrategy> strategies) {
        ProductTypeValidationStrategy simple = strategies.get(SimpleProductService.TYPE_CODE);
        if (simple != null) {
            return simple;
        }
        return strategies.values().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No ProductTypeValidationStrategy beans found"));
    }

    private ProductTypeValidationStrategy resolveCommon(Map<String, ProductTypeValidationStrategy> strategies) {
        ProductTypeValidationStrategy simple = strategies.get(SimpleProductService.TYPE_CODE);
        if (simple == null) {
            throw new IllegalStateException("Missing SIMPLE ProductTypeValidationStrategy");
        }
        return simple;
    }
}
