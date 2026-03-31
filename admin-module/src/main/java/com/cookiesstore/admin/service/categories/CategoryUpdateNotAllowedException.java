package com.cookiesstore.admin.service.categories;

public class CategoryUpdateNotAllowedException extends CategoryDomainException {

    public CategoryUpdateNotAllowedException(Long categoryId) {
        super("admin.categories.error.hasproducts");
    }
}
