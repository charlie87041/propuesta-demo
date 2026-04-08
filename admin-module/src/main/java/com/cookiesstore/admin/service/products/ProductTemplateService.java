package com.cookiesstore.admin.service.products;

import com.cookiesstore.admin.web.dto.products.ProductTemplateFieldForm;
import com.cookiesstore.admin.web.dto.products.UpdateProductTemplateForm;
import com.cookiesstore.common.entities.Product;
import com.cookiesstore.common.entities.ProductTemplate;
import com.cookiesstore.common.entities.ProductTemplateField;
import com.cookiesstore.common.entities.ProductTemplateFieldValue;
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

    public ProductTemplateService(
        ProductTemplateFieldRepository productTemplateFieldRepository,
        ProductTemplateFieldValueRepository productTemplateFieldValueRepository,
        ProductTemplateRepository productTemplateRepository
    ) {
        this.productTemplateFieldRepository = productTemplateFieldRepository;
        this.productTemplateFieldValueRepository = productTemplateFieldValueRepository;
        this.productTemplateRepository = productTemplateRepository;
    }

    public ProductTemplate findById(Long id) {
        return productTemplateRepository.findById(id)
            .orElseThrow(() -> new ProductTemplateNotFoundException(id));
    }

    public List<ProductTemplate> getAllProductTemplates() {
        return productTemplateRepository.findAll();
    }


    public Page<ProductTemplate> pageAllProductTemplates(Pageable pageable) {
        return productTemplateRepository.findAll(pageable);
    }


    public Page<ProductTemplate> pageAllProductTemplates(Pageable pageable, Specification<ProductTemplate> specification) {
        return productTemplateRepository.findAll(specification, pageable);
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
        String code = form.code().trim();
        var existingByCode = productTemplateRepository.findByCode(code);
        if (existingByCode.isPresent() && !existingByCode.get().getId().equals(productTemplateId)) {
            throw new ProductTemplateCodeExistsException(code);
        }

        productTemplate.setCode(code);
        productTemplate.setName(form.name().trim());
        productTemplate.setDescription(trimToNull(form.description()));
        productTemplate.setActive(form.active());
        productTemplateRepository.save(productTemplate);
        syncTemplateFields(form.fields(), productTemplate);
        return findById(productTemplateId);
    }

    @Transactional
    public ProductTemplate createProductTemplate(UpdateProductTemplateForm form) {
        String code = form.code().trim();
        if (productTemplateRepository.findByCode(code).isPresent()) {
            throw new ProductTemplateCodeExistsException(code);
        }

        ProductTemplate productTemplate = new ProductTemplate();
        productTemplate.setCode(code);
        productTemplate.setName(form.name().trim());
        productTemplate.setDescription(trimToNull(form.description()));
        productTemplate.setActive(form.active());
        productTemplateRepository.save(productTemplate);
        syncTemplateFields(form.fields(), productTemplate);
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
            String normalizedFieldKey = normalizeFieldKey(fieldForm.fieldKey());
            if (!normalizedFieldKeys.add(normalizedFieldKey)) {
                throw new ProductTemplateDuplicateFieldKeyException(normalizedFieldKey);
            }

            ProductTemplateField field;
            if (fieldForm.id() != null) {
                field = productTemplateFieldRepository.findById(fieldForm.id())
                    .orElseThrow(() -> new ProductTemplateFieldNotFoundException(fieldForm.id()));
                if (field.getTemplate() == null || !productTemplate.getId().equals(field.getTemplate().getId())) {
                    throw new ProductTemplateFieldInvalidReferenceException(fieldForm.id(), productTemplate.getId());
                }
                incomingExistingIds.add(fieldForm.id());
            } else {
                field = new ProductTemplateField();
            }

            field.setTemplate(productTemplate);
            field.setFieldKey(normalizedFieldKey);
            field.setLabel(fieldForm.label().trim());
            field.setFieldType(fieldForm.fieldType());
            field.setRequired(fieldForm.required());
            field.setDefaultValue(trimToNull(fieldForm.defaultValue()));
            field.setValidationRules(trimToNull(fieldForm.validationRules()));
            field.setSortOrder(Byte.toUnsignedInt(fieldForm.sortOrder()));
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
