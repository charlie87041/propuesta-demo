package com.cookiesstore.admin.web.dto.products;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProductForm(
    @NotBlank @Size(max = 80) String sku,
    @NotBlank @Size(max = 180) String name,
    @NotBlank @Size(max = 200) String slug,
    @Size(max = 5000) String description,
    @NotNull Long categoryId,
    @Size(max = 255) String mainImageUrl,
    @Size(max = 5000) String ingredients,
    @Size(max = 5000) String allergenInfo,
    @Size(max = 5000) String nutritionFacts,
    boolean active,
    boolean visible,
    List<Long> sourceIds,
    @NotNull @Min(0) Integer stockQuantity,
    @NotNull @Min(0) Integer lowStockThreshold,
    @NotNull Double price,
    Map<Long, Double> sourcePrices,
    Map<Long, Integer> sourceStockQuantities,
    Map<Long, Integer> sourceLowStockThresholds
) {
    public CreateProductForm {
        sourceIds = sourceIds == null ? List.of() : sourceIds;
        sourcePrices = sourcePrices == null ? new HashMap<>() : sourcePrices;
        sourceStockQuantities = sourceStockQuantities == null ? new HashMap<>() : sourceStockQuantities;
        sourceLowStockThresholds = sourceLowStockThresholds == null ? new HashMap<>() : sourceLowStockThresholds;
    }
}
