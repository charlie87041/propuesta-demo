package com.cookiesstore.admin.service.products;

import com.cookiesstore.admin.config.PricingProperties;
import com.cookiesstore.admin.service.support.AuthenticatedUserProvider;
import com.cookiesstore.admin.web.dto.products.CreateProductForm;
import com.cookiesstore.admin.web.dto.products.UpdateProductForm;
import com.cookiesstore.admin.web.dto.products.VariantProductForm;
import com.cookiesstore.common.entities.Product;
import com.cookiesstore.common.entities.ProductVariant;
import com.cookiesstore.common.repositories.CategoryRepository;
import com.cookiesstore.common.repositories.PriceRepository;
import com.cookiesstore.common.repositories.ProductRepository;
import com.cookiesstore.common.repositories.ProductSourceRepository;
import com.cookiesstore.common.repositories.ProductTemplateFieldRepository;
import com.cookiesstore.common.repositories.ProductTemplateFieldValueRepository;
import com.cookiesstore.common.repositories.ProductVariantRepository;
import com.cookiesstore.common.repositories.SourceRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class VariantProductService extends SimpleProductService {

    public static final String TYPE_CODE = "VARIANT";
    public static final String PARENT_TYPE_CODE = "VARIANT_PARENT";
    private static final boolean PARENT_IS_LISTABLE = true;
    private static final boolean PARENT_IS_SEARCHABLE = true;
    private static final boolean PARENT_IS_PURCHASABLE_ALONE = false;
    private static final boolean VARIANT_IS_LISTABLE = false;
    private static final boolean VARIANT_IS_SEARCHABLE = false;
    private static final boolean VARIANT_IS_PURCHASABLE_ALONE = false;

    protected final ProductVariantRepository productVariantRepository;
    protected final ProductRepository productRepository;

    public VariantProductService(
        ProductRepository productRepository,
        CategoryRepository categoryRepository,
        SourceRepository sourceRepository,
        PriceRepository priceRepository,
        ProductSourceRepository productSourceRepository,
        ProductTemplateFieldRepository productTemplateFieldRepository,
        ProductTemplateFieldValueRepository productTemplateFieldValueRepository,
        PricingProperties pricingProperties,
        AuthenticatedUserProvider authenticatedUserProvider,
        ProductVariantRepository productVariantRepository
    ) {
        super(
            productRepository,
            categoryRepository,
            sourceRepository,
            priceRepository,
            productSourceRepository,
            productTemplateFieldRepository,
            productTemplateFieldValueRepository,
            pricingProperties,
            authenticatedUserProvider
        );
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Override
    public String productTypeCode() {
        return TYPE_CODE;
    }

    @Override
    public Product getProduct(Long productId) {
        return productRepository
            .findByIdAndProductTypeCode(productId, TYPE_CODE)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    @Override
    public Product createProduct(CreateProductForm form) {
        Product parentProduct = super.createProduct(getVariantParentForm(form));
        List<CreateProductForm> variants = form.variantForms() == null ? List.of() : form.variantForms();

        for (CreateProductForm variantForm : variants) {
            VariantProductForm variantData = variantForm.variantData();

            Product variantProduct;
            if (variantData.variantProductId() != null) {
                Long existingVariantId = variantData.variantProductId();
                variantProduct = productRepository
                    .findByIdAndProductTypeCode(existingVariantId, TYPE_CODE)
                    .orElseThrow(() -> new VariantProductValidationException("admin.products.variant.invalid-reference"));
            } else {
                variantProduct = super.createProduct(getVariantForm(variantForm));
            }

            ProductVariant variant = new ProductVariant();
            variant.setDefaultVariant(variantData.defaultVariant());
            variant.setParentProduct(parentProduct);
            variant.setSortOrder(variantData.sortOrder());
            variant.setVariantProduct(variantProduct);
            productVariantRepository.save(variant);
        }

        return parentProduct;
    }

    @Override
    public UpdateProductForm buildUpdateProductForm(Long productId) {
        Product variantProduct = getProduct(productId);
        UpdateProductForm baseForm = super.buildUpdateProductForm(variantProduct.getId());
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
            VARIANT_IS_LISTABLE,
            VARIANT_IS_SEARCHABLE,
            VARIANT_IS_PURCHASABLE_ALONE,
            null,
            List.of()
        );
    }

    @Override
    public Product updateProduct(Long productId, UpdateProductForm form) {
        Product target = resolveVariantScopeProduct(productId);
        if (TYPE_CODE.equals(target.getProductTypeCode())) {
            UpdateProductForm variantForm = getVariantUpdateForm(form);
            return super.updateProduct(target.getId(), variantForm);
        }

        UpdateProductForm parentForm = getVariantParentUpdateForm(form);
        Product parent = super.updateProduct(target.getId(), parentForm);
        syncParentVariants(parent, form.variantForms());
        return parent;
    }

    @Override
    public void deleteProduct(Long productId) {
        Product target = resolveVariantScopeProduct(productId);

        if (PARENT_TYPE_CODE.equals(target.getProductTypeCode())) {
            List<ProductVariant> familyRelations = productVariantRepository.findByParentProductIdOrderBySortOrderAsc(target.getId());
            List<Long> variantIds = familyRelations.stream()
                .map(item -> item.getVariantProduct().getId())
                .toList();

            productVariantRepository.deleteByParentProductId(target.getId());
            for (Long variantId : variantIds) {
                super.deleteProduct(variantId);
            }
            super.deleteProduct(target.getId());
            return;
        }

        ProductVariant relation = productVariantRepository.findByVariantProductId(target.getId()).orElse(null);
        if (relation == null) {
            productVariantRepository.deleteByVariantProductId(target.getId());
            super.deleteProduct(target.getId());
            return;
        }

        Long parentId = relation.getParentProduct().getId();
        boolean wasDefault = relation.isDefaultVariant();

        productVariantRepository.deleteByVariantProductId(target.getId());
        super.deleteProduct(target.getId());

        List<ProductVariant> siblings = productVariantRepository.findByParentProductIdOrderBySortOrderAsc(parentId);
        if (siblings.isEmpty()) {
            // Keep the parent for audit/history, but make it unavailable with no variants.
            super.deactivateProduct(parentId);
            return;
        }

        if (wasDefault) {
            siblings.forEach(item -> item.setDefaultVariant(false));
            ProductVariant newDefault = siblings.get(0);
            newDefault.setDefaultVariant(true);
            productVariantRepository.saveAll(siblings);
        }
    }

    @Override
    public void enableProduct(Long productId) {
        Product target = resolveVariantScopeProduct(productId);

        if (TYPE_CODE.equals(target.getProductTypeCode())) {
            super.enableProduct(target.getId());
            return;
        }

        List<ProductVariant> familyRelations = productVariantRepository.findByParentProductIdOrderBySortOrderAsc(target.getId());
        super.enableProduct(target.getId());
        for (ProductVariant item : familyRelations) {
            super.enableProduct(item.getVariantProduct().getId());
        }
    }

    @Override
    public void deactivateProduct(Long productId) {
        Product target = resolveVariantScopeProduct(productId);

        if (TYPE_CODE.equals(target.getProductTypeCode())) {
            super.deactivateProduct(target.getId());
            return;
        }

        List<ProductVariant> familyRelations = productVariantRepository.findByParentProductIdOrderBySortOrderAsc(target.getId());
        super.deactivateProduct(target.getId());
        for (ProductVariant item : familyRelations) {
            super.deactivateProduct(item.getVariantProduct().getId());
        }
    }

    private Product resolveVariantScopeProduct(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

        String typeCode = product.getProductTypeCode();
        if (!TYPE_CODE.equals(typeCode) && !PARENT_TYPE_CODE.equals(typeCode)) {
            throw new ProductNotFoundException(productId);
        }
        return product;
    }

    protected CreateProductForm getVariantParentForm(CreateProductForm form) {
        return new CreateProductForm(
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
            PARENT_TYPE_CODE,
            List.of(),
            List.of(),
            form.sourcePrices(),
            form.sourceStockQuantities(),
            form.sourceLowStockThresholds(),
            form.templateValues(),
            PARENT_IS_LISTABLE,
            PARENT_IS_SEARCHABLE,
            PARENT_IS_PURCHASABLE_ALONE,
            null,
            List.of()
        );
    }

    protected CreateProductForm getVariantForm(CreateProductForm form) {
        return new CreateProductForm(
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
            List.of(),
            List.of(),
            form.sourcePrices(),
            form.sourceStockQuantities(),
            form.sourceLowStockThresholds(),
            form.templateValues(),
            VARIANT_IS_LISTABLE,
            VARIANT_IS_SEARCHABLE,
            VARIANT_IS_PURCHASABLE_ALONE,
            form.variantData(),
            List.of()
        );
    }

    protected UpdateProductForm getVariantParentUpdateForm(UpdateProductForm form) {
        return new UpdateProductForm(
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
            PARENT_TYPE_CODE,
            List.of(),
            List.of(),
            form.sourcePrices(),
            form.sourceStockQuantities(),
            form.sourceLowStockThresholds(),
            form.templateValues(),
            form.active(),
            form.visible(),
            PARENT_IS_LISTABLE,
            PARENT_IS_SEARCHABLE,
            PARENT_IS_PURCHASABLE_ALONE,
            null,
            List.of()
        );
    }

    protected UpdateProductForm getVariantUpdateForm(UpdateProductForm form) {
        return new UpdateProductForm(
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
            VARIANT_IS_LISTABLE,
            VARIANT_IS_SEARCHABLE,
            VARIANT_IS_PURCHASABLE_ALONE,
            null,
            List.of()
        );
    }

    private void syncParentVariants(Product parent, List<CreateProductForm> requestedVariants) {
        List<CreateProductForm> payload = requestedVariants == null ? List.of() : requestedVariants;
        List<ProductVariant> existing = productVariantRepository.findByParentProductIdOrderBySortOrderAsc(parent.getId());
        Map<Long, ProductVariant> existingByVariantId = existing.stream()
            .collect(Collectors.toMap(item -> item.getVariantProduct().getId(), Function.identity()));

        Set<Long> keepVariantIds = payload.stream()
            .map(entry -> entry.variantData())
            .filter(data -> data != null && data.variantProductId() != null)
            .map(data -> data.variantProductId())
            .collect(Collectors.toSet());

        for (CreateProductForm variantForm : payload) {
            VariantProductForm variantData = variantForm.variantData();
            if (variantData == null) {
                throw new VariantProductValidationException("admin.products.variant.data.required");
            }

            Product variantProduct;
            if (variantData.variantProductId() != null) {
                Long existingVariantId = variantData.variantProductId();
                ProductVariant linkedVariant = productVariantRepository.findByVariantProductId(existingVariantId)
                    .orElseThrow(() -> new VariantProductValidationException("admin.products.variant.invalid-reference"));

                if (!linkedVariant.getParentProduct().getId().equals(parent.getId())) {
                    throw new VariantProductValidationException("admin.products.variant.invalid-reference");
                }

                productRepository
                    .findByIdAndProductTypeCode(existingVariantId, TYPE_CODE)
                    .orElseThrow(() -> new VariantProductValidationException("admin.products.variant.invalid-reference"));

                variantProduct = super.updateProduct(existingVariantId, getVariantUpdateForm(variantForm));
            } else {
                variantProduct = super.createProduct(getVariantForm(variantForm));
                keepVariantIds.add(variantProduct.getId());
            }

            ProductVariant relation = existingByVariantId.get(variantProduct.getId());
            if (relation == null) {
                relation = new ProductVariant();
                relation.setParentProduct(parent);
                relation.setVariantProduct(variantProduct);
            }
            relation.setSortOrder(variantData.sortOrder());
            relation.setDefaultVariant(variantData.defaultVariant());
            productVariantRepository.save(relation);
        }

        for (ProductVariant relation : existing) {
            Long variantId = relation.getVariantProduct().getId();
            if (!keepVariantIds.contains(variantId)) {
                productVariantRepository.deleteByVariantProductId(variantId);
                super.deleteProduct(variantId);
            }
        }

        List<ProductVariant> persisted = productVariantRepository.findByParentProductIdOrderBySortOrderAsc(parent.getId());
        if (!persisted.isEmpty()) {
            boolean hasDefault = persisted.stream().anyMatch(ProductVariant::isDefaultVariant);
            if (!hasDefault) {
                ProductVariant first = persisted.get(0);
                first.setDefaultVariant(true);
                productVariantRepository.save(first);
            }
        } else {
            super.deactivateProduct(parent.getId());
        }
    }

    protected UpdateProductForm getVariantUpdateForm(CreateProductForm form) {
        return new UpdateProductForm(
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
            VARIANT_IS_LISTABLE,
            VARIANT_IS_SEARCHABLE,
            VARIANT_IS_PURCHASABLE_ALONE,
            null,
            List.of()
        );
    }
}
