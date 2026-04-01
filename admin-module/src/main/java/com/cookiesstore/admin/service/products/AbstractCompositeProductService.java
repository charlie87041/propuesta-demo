package com.cookiesstore.admin.service.products;

import com.cookiesstore.admin.config.PricingProperties;
import com.cookiesstore.admin.service.support.AuthenticatedUserProvider;
import com.cookiesstore.admin.web.dto.products.BundleComponentForm;
import com.cookiesstore.admin.web.dto.products.CreateProductForm;
import com.cookiesstore.admin.web.dto.products.PackageComponentForm;
import com.cookiesstore.admin.web.dto.products.UpdateProductForm;
import com.cookiesstore.common.repositories.CategoryRepository;
import com.cookiesstore.common.repositories.PriceRepository;
import com.cookiesstore.common.repositories.ProductRepository;
import com.cookiesstore.common.repositories.ProductSourceRepository;
import com.cookiesstore.common.repositories.SourceRepository;
import java.util.List;

public abstract class AbstractCompositeProductService extends SimpleProductService {

    protected AbstractCompositeProductService(
        ProductRepository productRepository,
        CategoryRepository categoryRepository,
        SourceRepository sourceRepository,
        PriceRepository priceRepository,
        ProductSourceRepository productSourceRepository,
        PricingProperties pricingProperties,
        AuthenticatedUserProvider authenticatedUserProvider
    ) {
        super(
            productRepository,
            categoryRepository,
            sourceRepository,
            priceRepository,
            productSourceRepository,
            pricingProperties,
            authenticatedUserProvider
        );
    }

    protected CreateProductForm buildCompositeCreateForm(
        CreateProductForm sourceForm,
        double resolvedPrice,
        String typeCode,
        List<BundleComponentForm> bundleComponents,
        List<PackageComponentForm> packageOptions,
        boolean isListable,
        boolean isSearchable,
        boolean isPurchasableAlone
    ) {
        return new CreateProductForm(
            sourceForm.sku(),
            sourceForm.name(),
            sourceForm.slug(),
            sourceForm.description(),
            sourceForm.categoryId(),
            sourceForm.mainImageUrl(),
            sourceForm.ingredients(),
            sourceForm.allergenInfo(),
            sourceForm.nutritionFacts(),
            sourceForm.active(),
            sourceForm.visible(),
            sourceForm.sourceIds(),
            sourceForm.stockQuantity(),
            sourceForm.lowStockThreshold(),
            resolvedPrice,
            typeCode,
            bundleComponents,
            packageOptions,
            sourceForm.sourcePrices(),
            sourceForm.sourceStockQuantities(),
            sourceForm.sourceLowStockThresholds(),
            isListable,
            isSearchable,
            isPurchasableAlone,
            null,
            List.of()
        );
    }

    protected UpdateProductForm buildCompositeUpdateForm(
        UpdateProductForm sourceForm,
        double resolvedPrice,
        String typeCode,
        List<BundleComponentForm> bundleComponents,
        List<PackageComponentForm> packageOptions
    ) {
        return new UpdateProductForm(
            sourceForm.sku(),
            sourceForm.name(),
            sourceForm.slug(),
            sourceForm.description(),
            sourceForm.categoryId(),
            sourceForm.mainImageUrl(),
            sourceForm.ingredients(),
            sourceForm.allergenInfo(),
            sourceForm.nutritionFacts(),
            sourceForm.sourceIds(),
            sourceForm.stockQuantity(),
            sourceForm.lowStockThreshold(),
            resolvedPrice,
            typeCode,
            bundleComponents,
            packageOptions,
            sourceForm.sourcePrices(),
            sourceForm.sourceStockQuantities(),
            sourceForm.sourceLowStockThresholds(),
            sourceForm.active(),
            sourceForm.visible(),
            sourceForm.isListable(),
            sourceForm.isPurchasable(),
            sourceForm.isPurchasableAlone(),
            sourceForm.variantData(),
            sourceForm.variantForms()
        );
    }

    protected UpdateProductForm mergeCompositionIntoUpdateForm(
        UpdateProductForm baseForm,
        String typeCode,
        List<BundleComponentForm> bundleComponents,
        List<PackageComponentForm> packageOptions
    ) {
        return new UpdateProductForm(
            baseForm.sku(),
            baseForm.name(),
            baseForm.slug(),
            baseForm.description(),
            baseForm.categoryId(),
            baseForm.mainImageUrl(),
            baseForm.ingredients(),
            baseForm.allergenInfo(),
            baseForm.nutritionFacts(),
            baseForm.sourceIds(),
            baseForm.stockQuantity(),
            baseForm.lowStockThreshold(),
            baseForm.price(),
            typeCode,
            bundleComponents,
            packageOptions,
            baseForm.sourcePrices(),
            baseForm.sourceStockQuantities(),
            baseForm.sourceLowStockThresholds(),
            baseForm.active(),
            baseForm.visible(),
            baseForm.isListable(),
            baseForm.isPurchasable(),
            baseForm.isPurchasableAlone(),
            baseForm.variantData(),
            baseForm.variantForms()
        );
    }
}
