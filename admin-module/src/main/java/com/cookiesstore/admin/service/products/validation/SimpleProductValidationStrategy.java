package com.cookiesstore.admin.service.products.validation;

import com.cookiesstore.admin.service.products.ProductPriceValidationException;
import com.cookiesstore.admin.service.products.SimpleProductService;
import com.cookiesstore.admin.web.dto.products.CreateProductForm;
import com.cookiesstore.admin.web.dto.products.UpdateProductForm;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SimpleProductValidationStrategy implements ProductTypeValidationStrategy {

    @Override
    public String supportedTypeCode() {
        return SimpleProductService.TYPE_CODE;
    }

    @Override
    public void validateCreate(CreateProductForm form) {
        validateMonetaryFields(form.price(), form.sourcePrices());
    }

    @Override
    public void validateUpdate(Long productId, UpdateProductForm form) {
        validateMonetaryFields(form.price(), form.sourcePrices());
    }

    private void validateMonetaryFields(BigDecimal basePrice, Map<Long, BigDecimal> sourcePrices) {
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ProductPriceValidationException("admin.products.error.price.invalid");
        }

        if (sourcePrices == null || sourcePrices.isEmpty()) {
            return;
        }

        for (BigDecimal sourcePrice : sourcePrices.values()) {
            if (sourcePrice == null) {
                continue;
            }
            if (sourcePrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ProductPriceValidationException("admin.products.error.source.price.invalid");
            }
        }
    }
}
