package com.cookiesstore.admin.service.products.validation;

import com.cookiesstore.admin.service.products.VariantProductService;
import com.cookiesstore.admin.service.products.VariantProductValidationException;
import com.cookiesstore.admin.web.dto.products.CreateProductForm;
import com.cookiesstore.admin.web.dto.products.UpdateProductForm;
import com.cookiesstore.admin.web.dto.products.VariantProductForm;
import com.cookiesstore.common.repositories.ProductRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class VariantProductValidationStrategy implements ProductTypeValidationStrategy {

    private final ProductRepository productRepository;

    public VariantProductValidationStrategy(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public String supportedTypeCode() {
        return VariantProductService.TYPE_CODE;
    }

    @Override
    public void validateCreate(CreateProductForm form) {
        validateVariantForms(form.variantForms());
    }

    @Override
    public void validateUpdate(Long productId, UpdateProductForm form) {
        boolean isParent = productRepository.findByIdAndProductTypeCode(productId, VariantProductService.PARENT_TYPE_CODE).isPresent();
        if (isParent) {
            validateVariantForms(form.variantForms());
        }
    }

    private void validateVariantForms(List<CreateProductForm> variantForms) {
        if (variantForms == null || variantForms.isEmpty()) {
            throw new VariantProductValidationException("admin.products.variant.forms.required");
        }

        int defaultCount = 0;
        Set<Long> existingVariantIds = new HashSet<>();
        Set<String> newVariantUnique = new HashSet<>();
        Set<Integer> sortOrders = new HashSet<>();

        for (CreateProductForm variantForm : variantForms) {
            if (variantForm == null) {
                throw new VariantProductValidationException("admin.products.variant.form.null-entry");
            }

            VariantProductForm variantData = variantForm.variantData();
            if (variantData == null) {
                throw new VariantProductValidationException("admin.products.variant.data.required");
            }

            if (variantData.sortOrder() < 0) {
                throw new VariantProductValidationException("admin.products.variant.sort.invalid");
            }
            if (!sortOrders.add(variantData.sortOrder())) {
                throw new VariantProductValidationException("admin.products.variant.sort.duplicate");
            }

            if (variantData.defaultVariant()) {
                defaultCount++;
            }

            if (variantData.variantProductId() != null) {
                Long variantId = variantData.variantProductId();
                if (!existingVariantIds.add(variantId)) {
                    throw new VariantProductValidationException("admin.products.variant.duplicate");
                }
                boolean exists = productRepository.findByIdAndProductTypeCode(variantId, VariantProductService.TYPE_CODE).isPresent();
                if (!exists) {
                    throw new VariantProductValidationException("admin.products.variant.invalid-reference");
                }
            } else {
                if (variantForm.variantForms() != null && !variantForm.variantForms().isEmpty()) {
                    throw new VariantProductValidationException("admin.products.variant.nested-not-supported");
                }
                if (!VariantProductService.TYPE_CODE.equals(variantForm.productTypeCode())) {
                    throw new VariantProductValidationException("admin.products.variant.type.invalid");
                }
                String uniqueKey = safe(variantForm.sku()) + "|" + safe(variantForm.slug());
                if (!newVariantUnique.add(uniqueKey)) {
                    throw new VariantProductValidationException("admin.products.variant.duplicate");
                }
            }
        }

        if (defaultCount > 1) {
            throw new VariantProductValidationException("admin.products.variant.default.multiple");
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
}
