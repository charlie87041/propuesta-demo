package com.cookiesstore.admin.service.products.validation;

import com.cookiesstore.admin.web.dto.products.CreateProductForm;
import com.cookiesstore.admin.web.dto.products.UpdateProductForm;

public interface ProductTypeValidationStrategy {
    String supportedTypeCode();

    void validateCreate(CreateProductForm form);

    void validateUpdate(Long productId, UpdateProductForm form);
}
