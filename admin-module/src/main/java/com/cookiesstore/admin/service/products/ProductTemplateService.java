package com.cookiesstore.admin.service.products;

import com.cookiesstore.admin.web.dto.products.ProductTemplateFieldForm;
import com.cookiesstore.admin.web.dto.products.UpdateProductTemplateForm;
import com.cookiesstore.common.entities.Product;
import com.cookiesstore.common.entities.ProductTemplate;
import com.cookiesstore.common.entities.ProductTemplateField;
import com.cookiesstore.common.entities.ProductTemplateFieldValue;
import com.cookiesstore.common.repositories.CategoryRepository;
import com.cookiesstore.common.repositories.ProductRepository;
import com.cookiesstore.common.repositories.ProductTemplateFieldRepository;
import com.cookiesstore.common.repositories.ProductTemplateFieldValueRepository;
import com.cookiesstore.common.repositories.ProductTemplateRepository;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductTemplateService {

    private final ProductTemplateFieldRepository productTemplateFieldRepository;
    private final ProductTemplateFieldValueRepository productTemplateFieldValueRepository;
    private final ProductTemplateRepository productTemplateRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductTemplateService(
        ProductTemplateFieldRepository productTemplateFieldRepository,
        ProductTemplateFieldValueRepository productTemplateFieldValueRepository,
        ProductTemplateRepository productTemplateRepository,
        ProductRepository productRepository,
        CategoryRepository categoryRepository
    ) {
        this.productTemplateFieldRepository = productTemplateFieldRepository;
        this.productTemplateFieldValueRepository = productTemplateFieldValueRepository;
        this.productTemplateRepository = productTemplateRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public ProductTemplate findById(Long id) {
        return productTemplateRepository.findById(id)
            .orElseThrow(() -> new ProductTemplateNotFoundException(id));
    }

    public List<ProductTemplate> getAllProductTemplates() {
        return productTemplateRepository.findByLatestTrue(org.springframework.data.domain.Sort.by("name"));
    }


    public Page<ProductTemplate> pageAllProductTemplates(Pageable pageable) {
        return productTemplateRepository.findByLatestTrue(pageable);
    }


    public Page<ProductTemplate> pageAllProductTemplates(Pageable pageable, Specification<ProductTemplate> specification) {
        Specification<ProductTemplate> latestSpec = (root, query, builder) -> builder.isTrue(root.get("latest"));
        Specification<ProductTemplate> combined = specification == null
            ? latestSpec
            : Specification.where(latestSpec).and(specification);
        return productTemplateRepository.findAll(combined, pageable);
    }

    public void enableProductTemplate(Long productTemplateId) {
        ProductTemplate productTemplate = findById(productTemplateId);
        productTemplate.setActive(true);
        productTemplateRepository.save(productTemplate);
    }

    public void deactivateProductTemplate(Long productTemplateId) {
        ProductTemplate productTemplate = findById(productTemplateId);
        productTemplate.setActive(false);
        productTemplateRepository.save(productTemplate);
    }

    public Map<String, String> buildTemplateValuesMap(Long productId) {
        return productTemplateFieldValueRepository.findByProductId(productId)
            .stream()
            .filter(value -> value.getTemplateField() != null && value.getTemplateField().getFieldKey() != null)
            .collect(
                java.util.stream.Collectors.toMap(
                    value -> value.getTemplateField().getFieldKey(),
                    ProductTemplateFieldValue::getFieldValue,
                    (left, right) -> right,
                    LinkedHashMap::new
                )
            );
    }

    @Transactional
    public ProductTemplate updateProductTemplate(UpdateProductTemplateForm form, Long productTemplateId) {
        ProductTemplate productTemplate = findById(productTemplateId);
        String code = form.getCode().trim();
        boolean inUse = productRepository.existsByTemplateId(productTemplateId);

        if (inUse) {
            if (!productTemplate.getCode().equals(code)) {
                throw new ProductTemplateCodeLockedException(code);
            }
            ProductTemplate latestTemplate = productTemplateRepository.findTopByCodeOrderByVersionDesc(productTemplate.getCode())
                .orElse(productTemplate);
            int nextVersion = latestTemplate.getVersion() + 1;
            productTemplateRepository.findByCodeAndLatestTrue(productTemplate.getCode())
                .ifPresent(currentLatest -> {
                    currentLatest.setLatest(false);
                    productTemplateRepository.saveAndFlush(currentLatest);
                });

            ProductTemplate newTemplate = new ProductTemplate();
            newTemplate.setCode(productTemplate.getCode());
            newTemplate.setName(form.getName().trim());
            newTemplate.setDescription(trimToNull(form.getDescription()));
            newTemplate.setActive(form.isActive());
            newTemplate.setVersion(nextVersion);
            newTemplate.setLatest(true);
            productTemplateRepository.save(newTemplate);
            syncTemplateFields(form.getFields(), newTemplate);
            updateCategoriesDefaultTemplate(productTemplateId, newTemplate);
            return newTemplate;
        }

        if (!productTemplate.getCode().equals(code) && productTemplateRepository.existsByCode(code)) {
            throw new ProductTemplateCodeExistsException(code);
        }

        productTemplate.setCode(code);
        productTemplate.setName(form.getName().trim());
        productTemplate.setDescription(trimToNull(form.getDescription()));
        productTemplate.setActive(form.isActive());
        productTemplateRepository.save(productTemplate);
        syncTemplateFields(form.getFields(), productTemplate);
        return findById(productTemplateId);
    }

    @Transactional
    public ProductTemplate createProductTemplate(UpdateProductTemplateForm form) {
        String code = form.getCode().trim();
        if (productTemplateRepository.existsByCode(code)) {
            throw new ProductTemplateCodeExistsException(code);
        }

        ProductTemplate productTemplate = new ProductTemplate();
        productTemplate.setCode(code);
        productTemplate.setName(form.getName().trim());
        productTemplate.setDescription(trimToNull(form.getDescription()));
        productTemplate.setActive(form.isActive());
        productTemplate.setVersion(1);
        productTemplate.setLatest(true);
        productTemplateRepository.save(productTemplate);
        syncTemplateFields(form.getFields(), productTemplate);
        return findById(productTemplate.getId());
    }

    public boolean deleteProductTemplate(Long productTemplateId) {
        ProductTemplate productTemplate = findById(productTemplateId);
        if (!productTemplate.getProducts().isEmpty()) {
            throw new ProductTemplateInUseException(productTemplateId);
        }
    
        productTemplateRepository.delete(productTemplate);
        return true;
    }

    public void syncTemplateFieldValues(Product product, Map<String, String> requestedValues) {
        ProductTemplate template = product.getTemplate();
        if (template == null || template.getId() == null) {
            productTemplateFieldValueRepository.deleteByProductId(product.getId());
            return;
        }

        Map<String, String> values = requestedValues == null ? Map.of() : requestedValues;
        List<ProductTemplateField> templateFields = productTemplateFieldRepository.findByTemplateIdOrderBySortOrderAsc(template.getId());
        Map<Long, ProductTemplateFieldValue> existingByFieldId = productTemplateFieldValueRepository.findByProductId(product.getId())
            .stream()
            .filter(value -> value.getTemplateField() != null && value.getTemplateField().getId() != null)
            .collect(
                java.util.stream.Collectors.toMap(
                    value -> value.getTemplateField().getId(),
                    value -> value,
                    (left, right) -> left
                )
            );

        List<ProductTemplateFieldValue> toSave = new java.util.ArrayList<>();
        List<ProductTemplateFieldValue> toDelete = new java.util.ArrayList<>();

        for (ProductTemplateField field : templateFields) {
            String normalizedValue = normalizeTemplateFieldValue(values.get(field.getFieldKey()));
            if (field.isRequired() && !StringUtils.hasText(normalizedValue)) {
                throw new ProductTemplateValidationException(field.getLabel());
            }
            ProductTemplateFieldValue existing = existingByFieldId.remove(field.getId());

            if (!StringUtils.hasText(normalizedValue)) {
                if (existing != null) {
                    toDelete.add(existing);
                }
                continue;
            }

            if (existing != null) {
                existing.setFieldValue(normalizedValue);
                toSave.add(existing);
                continue;
            }

            ProductTemplateFieldValue created = new ProductTemplateFieldValue();
            created.setProduct(product);
            created.setTemplateField(field);
            created.setFieldValue(normalizedValue);
            toSave.add(created);
        }

        if (!existingByFieldId.isEmpty()) {
            toDelete.addAll(existingByFieldId.values());
        }

        if (!toDelete.isEmpty()) {
            productTemplateFieldValueRepository.deleteAllInBatch(toDelete);
        }
        if (!toSave.isEmpty()) {
            productTemplateFieldValueRepository.saveAll(toSave);
        }
    }


    private void syncTemplateFields(List<ProductTemplateFieldForm> fields, ProductTemplate productTemplate)
    {
        List<ProductTemplateField> existingFields = productTemplateFieldRepository.findByTemplateIdOrderBySortOrderAsc(productTemplate.getId());
        Set<String> normalizedFieldKeys = new LinkedHashSet<>();
        List<ProductTemplateField> toSave = new java.util.ArrayList<>();
        Set<Long> incomingExistingIds = new java.util.HashSet<>();
        for (var fieldForm : fields) {
            String normalizedFieldKey = normalizeFieldKey(fieldForm.getFieldKey());
            if (!normalizedFieldKeys.add(normalizedFieldKey)) {
                throw new ProductTemplateDuplicateFieldKeyException(normalizedFieldKey);
            }

            ProductTemplateField field;
            if (fieldForm.getId() != null) {
                System.out.println("field "+ fieldForm.getId());
                field = productTemplateFieldRepository.findById(fieldForm.getId())
                    .orElseThrow(() -> new ProductTemplateFieldNotFoundException(fieldForm.getId()));
                if (!productTemplate.getId().equals(field.getTemplate().getId())) {
                    field = new ProductTemplateField();
                } else{
                    incomingExistingIds.add(fieldForm.getId());
                }
            } else {
                field = new ProductTemplateField();
            }

            field.setTemplate(productTemplate);
            field.setFieldKey(normalizedFieldKey);
            field.setLabel(fieldForm.getLabel().trim());
            field.setFieldType(fieldForm.getFieldType());
            field.setRequired(fieldForm.isRequired());
            field.setDefaultValue(trimToNull(fieldForm.getDefaultValue()));
            field.setValidationRules(trimToNull(fieldForm.getValidationRules()));
            field.setSortOrder(Byte.toUnsignedInt(fieldForm.getSortOrder()));
            toSave.add(field);
        }

        List<ProductTemplateField> toDelete = existingFields.stream()
            .filter(existing -> !incomingExistingIds.contains(existing.getId()))
            .toList();

        if (!toDelete.isEmpty()) {
            productTemplateFieldRepository.deleteAllInBatch(toDelete);
        }
        if (!toSave.isEmpty()) {
            productTemplateFieldRepository.saveAll(toSave);
        }
    }

    private void updateCategoriesDefaultTemplate(Long previousTemplateId, ProductTemplate newTemplate) {
        var categories = categoryRepository.findByDefaultTemplateId(previousTemplateId);
        if (categories.isEmpty()) {
            return;
        }
        for (var category : categories) {
            category.setDefaultTemplate(newTemplate);
        }
        categoryRepository.saveAll(categories);
    }


    private String normalizeTemplateFieldValue(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        return raw.trim();
    }

    private String normalizeFieldKey(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "";
        }
        return raw.trim();
    }

    private String trimToNull(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        return raw.trim();
    }
}
