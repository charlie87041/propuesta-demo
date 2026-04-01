package com.cookiesstore.admin.service.products;

import com.cookiesstore.admin.service.products.validation.ProductTypeValidationFactory;
import com.cookiesstore.admin.web.dto.products.CreateProductForm;
import com.cookiesstore.admin.web.dto.products.UpdateProductForm;
import com.cookiesstore.common.entities.Product;
import com.cookiesstore.common.repositories.ProductRepository;
import com.cookiesstore.common.services.products.ProductService;
import com.cookiesstore.common.services.products.ProductTypeService;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Primary
@Transactional
public class DelegatingProductService implements ProductService<CreateProductForm, UpdateProductForm> {

    private final ProductRepository productRepository;
    private final ProductTypeValidationFactory validationFactory;
    private final Map<String, ProductTypeService<CreateProductForm, UpdateProductForm>> servicesByTypeCode;
    private final ProductTypeService<CreateProductForm, UpdateProductForm> defaultService;

    public DelegatingProductService(
        ProductRepository productRepository,
        ProductTypeValidationFactory validationFactory,
        List<ProductTypeService<CreateProductForm, UpdateProductForm>> productTypeServices
    ) {
        this.productRepository = productRepository;
        this.validationFactory = validationFactory;
        this.servicesByTypeCode = productTypeServices.stream()
            .collect(Collectors.toMap(ProductTypeService::productTypeCode, Function.identity()));
        this.defaultService = resolveDefaultService(servicesByTypeCode);
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProduct(Long productId) {
        Product product = findExistingProduct(productId);
        return resolveByTypeCode(product.getProductTypeCode()).getProduct(productId);
    }

    @Override
    public Product createProduct(CreateProductForm form) {
        String requestedTypeCode = form == null ? null : form.productTypeCode();
        validationFactory.validateCreate(requestedTypeCode, form);
        return resolveByTypeCode(requestedTypeCode).createProduct(form);
    }

    @Override
    @Transactional(readOnly = true)
    public UpdateProductForm buildUpdateProductForm(Long productId) {
        Product product = findExistingProduct(productId);
        return resolveByTypeCode(product.getProductTypeCode()).buildUpdateProductForm(productId);
    }

    @Override
    public Product updateProduct(Long productId, UpdateProductForm form) {
        Product product = findExistingProduct(productId);
        validationFactory.validateUpdate(product.getProductTypeCode(), productId, form);
        return resolveByTypeCode(product.getProductTypeCode()).updateProduct(productId, form);
    }

    @Override
    public void deleteProduct(Long productId) {
        Product product = findExistingProduct(productId);
        resolveByTypeCode(product.getProductTypeCode()).deleteProduct(productId);
    }

    @Override
    public void enableProduct(Long productId) {
        Product product = findExistingProduct(productId);
        resolveByTypeCode(product.getProductTypeCode()).enableProduct(productId);
    }

    @Override
    public void deactivateProduct(Long productId) {
        Product product = findExistingProduct(productId);
        resolveByTypeCode(product.getProductTypeCode()).deactivateProduct(productId);
    }

    private Product findExistingProduct(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private ProductTypeService<CreateProductForm, UpdateProductForm> resolveByTypeCode(String typeCode) {
        if (typeCode != null) {
            ProductTypeService<CreateProductForm, UpdateProductForm> service = servicesByTypeCode.get(typeCode);
            if (service != null) {
                return service;
            }
            if (VariantProductService.PARENT_TYPE_CODE.equals(typeCode)) {
                ProductTypeService<CreateProductForm, UpdateProductForm> variantService = servicesByTypeCode.get(VariantProductService.TYPE_CODE);
                if (variantService != null) {
                    return variantService;
                }
            }
        }
        return defaultService;
    }

    private ProductTypeService<CreateProductForm, UpdateProductForm> resolveDefaultService(
        Map<String, ProductTypeService<CreateProductForm, UpdateProductForm>> services
    ) {
        ProductTypeService<CreateProductForm, UpdateProductForm> simpleService = services.get(SimpleProductService.TYPE_CODE);
        if (simpleService != null) {
            return simpleService;
        }

        return services.values().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No ProductTypeService beans found"));
    }
}
