package com.cookiesstore.admin.web.dto.products;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateProductForm(
    @NotBlank @Size(max = 80) String sku,
    @NotBlank @Size(max = 180) String name,
    @NotBlank @Size(max = 200) String slug,
    @Size(max = 5000) String description,
    @NotNull Long categoryId,
    @Size(max = 255) String mainImageUrl,
    List<Long> sourceIds,
    @NotNull @Min(0) Integer stockQuantity,
    @NotNull @Min(0) Integer lowStockThreshold,
    @NotNull Double price,
    String productTypeCode,
    List<BundleComponentForm> bundleComponents,
    List<PackageComponentForm> packageOptions,
    Map<Long, Double> sourcePrices,
    Map<Long, Integer> sourceStockQuantities,
    Map<Long, Integer> sourceLowStockThresholds,
    Map<String, String> templateValues,
    boolean active,
    boolean visible,
    boolean isListable,
    boolean isPurchasable,
    boolean isPurchasableAlone,
    VariantProductForm variantData,
    List<CreateProductForm> variantForms
) {
    public UpdateProductForm {
        productTypeCode = (productTypeCode == null || productTypeCode.isBlank()) ? "SIMPLE" : productTypeCode.trim().toUpperCase();
        sourceIds = sourceIds == null ? List.of() : sourceIds;
        bundleComponents = bundleComponents == null ? List.of() : bundleComponents;
        packageOptions = packageOptions == null ? List.of() : packageOptions;
        sourcePrices = sourcePrices == null ? new HashMap<>() : sourcePrices;
        sourceStockQuantities = sourceStockQuantities == null ? new HashMap<>() : sourceStockQuantities;
        sourceLowStockThresholds = sourceLowStockThresholds == null ? new HashMap<>() : sourceLowStockThresholds;
        templateValues = templateValues == null ? new HashMap<>() : templateValues;
        variantForms = variantForms == null ? List.of() : variantForms;
    }
}
