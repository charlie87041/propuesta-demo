package com.cookiesstore.admin.service.categories;

public class CategorySlugExistsException extends CategoryDomainException {

    public CategorySlugExistsException(String slug) {
        super("admin.categories.error.slug.exists");
    }
}
