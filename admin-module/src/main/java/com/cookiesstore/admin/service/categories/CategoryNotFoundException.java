package com.cookiesstore.admin.service.categories;

public class CategoryNotFoundException extends CategoryDomainException {

    public CategoryNotFoundException(Long categoryId) {
        super("admin.categories.flash.notFound");
    }
}
