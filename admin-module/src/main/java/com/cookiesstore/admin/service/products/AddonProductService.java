package com.cookiesstore.admin.service.products;

import com.cookiesstore.admin.service.support.AuthenticatedUserProvider;
import com.cookiesstore.admin.web.dto.products.CreateProductForm;
import com.cookiesstore.admin.web.dto.products.UpdateProductForm;
import com.cookiesstore.common.entities.Product;
import com.cookiesstore.common.repositories.CategoryRepository;
import com.cookiesstore.common.repositories.ProductRepository;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AddonProductService extends SimpleProductService {

    public static final String TYPE_CODE = "ADD_ON";

    public AddonProductService(
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

    @Override
    public String productTypeCode() {
        return TYPE_CODE;
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProduct(Long productId) {
        return productRepository
            .findByIdAndProductTypeCode(productId, TYPE_CODE)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    @Override
    @Transactional(readOnly = true)
    public UpdateProductForm buildUpdateProductForm(Long productId) {
        UpdateProductForm baseForm = super.buildUpdateProductForm(productId);
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
            TYPE_CODE,
            List.of(),
            List.of(),
            baseForm.sourcePrices(),
            baseForm.sourceStockQuantities(),
            baseForm.sourceLowStockThresholds(),
            baseForm.templateValues(),
            baseForm.active(),
            baseForm.visible(),
            false,
            false,
            false,
            null,
            List.of()
        );
    }

    @Override
    public Product createProduct(CreateProductForm form) {
        var addonProductForm = new CreateProductForm(
            form.sku(),
            form.name(),
            form.slug(),
            form.description(),
            form.categoryId(),
            form.mainImageUrl(),
            form.active(),
            form.visible(),
            form.sourceIds(),
            form.stockQuantity(),
            form.lowStockThreshold(),
            form.price(),
            TYPE_CODE,
            List.of(), // Bundle components not supported for addon products
            List.of(), // Package options not supported for addon products
            form.sourcePrices(),
            form.sourceStockQuantities(),
            form.sourceLowStockThresholds(),
            form.templateValues(),
            false,
            false,
            false,
            null,
            List.of()
        );
        Product created = super.createProduct(addonProductForm);
        created.setListable(false);
        created.setSearchable(false);
        created.setPurchasableAlone(false);
        return productRepository.save(created);
    }

    @Override
    public Product updateProduct(Long productId, UpdateProductForm form) {
        Product addon = getProduct(productId);
        UpdateProductForm addonForm = new UpdateProductForm(
            form.sku(),
            form.name(),
            form.slug(),
            form.description(),
            form.categoryId(),
            form.mainImageUrl(),
            form.sourceIds(),
            form.stockQuantity(),
            form.lowStockThreshold(),
            form.price(),
            TYPE_CODE,
            List.of(),
            List.of(),
            form.sourcePrices(),
            form.sourceStockQuantities(),
            form.sourceLowStockThresholds(),
            form.templateValues(),
            form.active(),
            form.visible(),
            false,
            false,
            false,
            null,
            List.of()
        );

        Product updated = super.updateProduct(addon.getId(), addonForm);
        updated.setProductTypeCode(TYPE_CODE);
        updated.setListable(false);
        updated.setSearchable(false);
        updated.setPurchasableAlone(false);
        return productRepository.save(updated);
    }

}
