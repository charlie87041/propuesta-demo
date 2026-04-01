package com.cookiesstore.admin.web.dto.products;

import com.cookiesstore.common.entities.ProductComponentPriceMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record BundleComponentForm(
    @NotNull Long childProductId,
    Long sourceId,
    @NotNull @DecimalMin(value = "0.001") BigDecimal quantity,
    @NotNull ProductComponentPriceMode unitPriceMode,
    @DecimalMin(value = "0.00") BigDecimal unitPriceOverride
) {
}
