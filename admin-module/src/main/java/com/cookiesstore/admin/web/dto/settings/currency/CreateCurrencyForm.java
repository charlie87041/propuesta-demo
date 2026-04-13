package com.cookiesstore.admin.web.dto.settings.currency;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCurrencyForm(
    @NotBlank @Size(max = 3) String code,
    @NotBlank @Size(max = 80) String name,
    @NotBlank @Size(max = 8) String symbol,
    @Min(value = 0) @Max(value = 4) int fractionDigits,
    boolean active


) {
}
