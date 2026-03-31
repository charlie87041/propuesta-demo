package com.cookiesstore.admin.service.categories;

public class CategoryUniqueConstraintException extends CategoryDomainException {

    public CategoryUniqueConstraintException() {
        super("admin.categories.error.unique");
    }
}
