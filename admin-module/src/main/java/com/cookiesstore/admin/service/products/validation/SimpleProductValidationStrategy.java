package com.cookiesstore.admin.service.products.validation;

import com.cookiesstore.admin.service.products.SimpleProductService;
import com.cookiesstore.admin.web.dto.products.CreateProductForm;
import com.cookiesstore.admin.web.dto.products.UpdateProductForm;
import org.springframework.stereotype.Component;

@Component
public class SimpleProductValidationStrategy implements ProductTypeValidationStrategy {

    @Override
    public String supportedTypeCode() {
        return SimpleProductService.TYPE_CODE;
    }

    @Override
    public void validateCreate(CreateProductForm form) {
        // No type-specific validations for SIMPLE products at this layer.
    }

    @Override
    public void validateUpdate(Long productId, UpdateProductForm form) {
        // No type-specific validations for SIMPLE products at this layer.
    }
}
