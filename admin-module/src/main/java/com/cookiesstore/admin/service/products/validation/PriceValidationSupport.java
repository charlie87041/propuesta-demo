package com.cookiesstore.admin.service.products.validation;

import com.cookiesstore.admin.service.products.ProductPriceValidationException;
import com.cookiesstore.common.util.MoneyConversion;
import java.math.BigDecimal;

public final class PriceValidationSupport {

    private PriceValidationSupport() {
    }

    public static long toMinorOrThrow(BigDecimal amount, int fractionDigits) {
        try {
            return MoneyConversion.toMinor(amount, Math.max(fractionDigits, 0));
        } catch (IllegalArgumentException ex) {
            throw new ProductPriceValidationException("admin.products.error.price.range");
        }
    }
}