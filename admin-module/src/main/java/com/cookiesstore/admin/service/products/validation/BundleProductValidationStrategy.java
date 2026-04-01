package com.cookiesstore.admin.service.products.validation;

import com.cookiesstore.admin.service.products.BundleComponentInvalidException;
import com.cookiesstore.admin.service.products.BundleComponentsRequiredException;
import com.cookiesstore.admin.service.products.BundleProductService;
import com.cookiesstore.admin.web.dto.products.BundleComponentForm;
import com.cookiesstore.admin.web.dto.products.CreateProductForm;
import com.cookiesstore.admin.web.dto.products.UpdateProductForm;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class BundleProductValidationStrategy implements ProductTypeValidationStrategy {

    @Override
    public String supportedTypeCode() {
        return BundleProductService.TYPE_CODE;
    }

    @Override
    public void validateCreate(CreateProductForm form) {
        List<BundleComponentForm> components = form.bundleComponents();
        validateComponentsRequired(components);
        validateNoDuplicateComponents(components);
        validateComponentFields(components);
    }

    @Override
    public void validateUpdate(Long productId, UpdateProductForm form) {
        List<BundleComponentForm> components = form.bundleComponents();
        validateComponentsRequired(components);
        validateNoDuplicateComponents(components);
        validateComponentFields(components);
        validateNoSelfReference(productId, components);
    }

    private void validateComponentsRequired(List<BundleComponentForm> components) {
        if (components == null || components.isEmpty()) {
            throw new BundleComponentsRequiredException();
        }
    }

    private void validateNoDuplicateComponents(List<BundleComponentForm> components) {
        Set<String> keys = new HashSet<>();
        for (BundleComponentForm component : components) {
            String key = component.childProductId() + "|" + component.sourceId();
            if (!keys.add(key)) {
                throw new BundleComponentInvalidException("admin.products.bundle.components.duplicate");
            }
        }
    }

    private void validateComponentFields(List<BundleComponentForm> components) {
        for (BundleComponentForm component : components) {
            if (component == null || component.childProductId() == null) {
                throw new BundleComponentInvalidException("admin.products.bundle.components.invalid-product");
            }
            if (component.quantity() == null || component.quantity().signum() <= 0) {
                throw new BundleComponentInvalidException("admin.products.bundle.components.invalid-quantity");
            }
            if (component.unitPriceMode() == null) {
                throw new BundleComponentInvalidException("admin.products.bundle.components.invalid-price-mode");
            }
        }
    }

    private void validateNoSelfReference(Long bundleId, List<BundleComponentForm> components) {
        if (bundleId == null) {
            return;
        }
        boolean hasSelfReference = components.stream()
            .anyMatch(component -> component.childProductId() != null && component.childProductId().equals(bundleId));
        if (hasSelfReference) {
            throw new BundleComponentInvalidException("admin.products.bundle.components.self-reference");
        }
    }
}
