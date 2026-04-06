package com.cookiesstore.admin.service.categories;

import com.cookiesstore.admin.web.dto.categories.CreateCategoryForm;
import com.cookiesstore.admin.web.dto.categories.UpdateCategoryForm;
import com.cookiesstore.common.entities.Category;
import com.cookiesstore.common.entities.ProductTemplate;
import com.cookiesstore.common.repositories.CategoryRepository;
import com.cookiesstore.common.repositories.ProductRepository;
import com.cookiesstore.common.repositories.ProductTemplateRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductTemplateRepository productTemplateRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository, ProductTemplateRepository productTemplateRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productTemplateRepository = productTemplateRepository;
    }

    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new CategoryNotFoundException(categoryId));
        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    }

    public Category createCategory(CreateCategoryForm form) {
        if (categoryRepository.findByCode(form.code()).isPresent()) {
            throw new CategoryCodeExistsException(form.code());
        }
        if (categoryRepository.findBySlug(form.slug()).isPresent()) {
            throw new CategorySlugExistsException(form.slug());
        }
        var productTemplate = productTemplateRepository.findById(form.productTemplateId())
            .orElseThrow(() -> new ProductTemplateNotFoundException(form.productTemplateId()));

        Category category = new Category();
        category.setCode(form.code().trim());
        category.setName(form.name().trim());
        category.setSlug(form.slug().trim());
        category.setDescription(form.description());
        category.setSortOrder(form.sortOrder());
        category.setActive(form.active());
        category.setDefaultTemplate(productTemplate);

        try {
            return categoryRepository.save(category);
        } catch (DataIntegrityViolationException ex) {
            throw new CategoryUniqueConstraintException();
        }
    }

    public Category updateCategory(Long categoryId, UpdateCategoryForm form) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        if (productRepository.existsByCategoryId(categoryId)) {
            throw new CategoryUpdateNotAllowedException(categoryId);
        }

        var existingByCode = categoryRepository.findByCode(form.code());
        if (existingByCode.isPresent() && !existingByCode.get().getId().equals(categoryId)) {
            throw new CategoryCodeExistsException(form.code());
        }

        var existingBySlug = categoryRepository.findBySlug(form.slug());
        if (existingBySlug.isPresent() && !existingBySlug.get().getId().equals(categoryId)) {
            throw new CategorySlugExistsException(form.slug());
        }

        var productTemplate = productTemplateRepository.findById(form.productTemplateId())
            .orElseThrow(() -> new ProductTemplateNotFoundException(form.productTemplateId()));

        category.setCode(form.code().trim());
        category.setName(form.name().trim());
        category.setSlug(form.slug().trim());
        category.setDescription(form.description());
        category.setSortOrder(form.sortOrder());
        category.setActive(form.active());
        category.setDefaultTemplate(productTemplate);

        try {
            return categoryRepository.save(category);
        } catch (DataIntegrityViolationException ex) {
            throw new CategoryUniqueConstraintException();
        }
    }

    public void enableCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new CategoryNotFoundException(categoryId));
        category.setActive(true);
        categoryRepository.save(category);
    }

    public void deactivateCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new CategoryNotFoundException(categoryId));
        category.setActive(false);
        categoryRepository.save(category);
    }
}
