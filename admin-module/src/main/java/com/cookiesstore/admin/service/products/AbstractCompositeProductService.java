package com.cookiesstore.admin.service.products;

import com.cookiesstore.admin.service.support.AuthenticatedUserProvider;
import com.cookiesstore.admin.web.dto.products.BundleComponentForm;
import com.cookiesstore.admin.web.dto.products.CreateProductForm;
import com.cookiesstore.admin.web.dto.products.PackageComponentForm;
import com.cookiesstore.admin.web.dto.products.UpdateProductForm;
import com.cookiesstore.common.repositories.CategoryRepository;
import com.cookiesstore.common.repositories.ProductRepository;
import java.math.BigDecimal;
import java.util.List;

public abstract class AbstractCompositeProductService extends SimpleProductService {

    protected AbstractCompositeProductService(
        ProductRepository productRepository,
        CategoryRepository categoryRepository,
        ProductPriceService productPriceService,
        ProductSourceService productSourceService,
        ProductTemplateService productTemplateService,
        AuthenticatedUserProvider authenticatedUserProvider
    ) {
        super(
            productRepository,
            categoryRepository,
            productPriceService,
            productSourceService,
            productTemplateService,
            authenticatedUserProvider
        );
    }

    protected CreateProductForm buildCompositeCreateForm(
        CreateProductForm sourceForm,
        BigDecimal resolvedPrice,
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
            sourceForm.templateValues(),
            isListable,
            isSearchable,
            isPurchasableAlone,
            null,
            List.of()
        );
    }

    protected UpdateProductForm buildCompositeUpdateForm(
        UpdateProductForm sourceForm,
        BigDecimal resolvedPrice,
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
            sourceForm.templateValues(),
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
            baseForm.templateValues(),
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
