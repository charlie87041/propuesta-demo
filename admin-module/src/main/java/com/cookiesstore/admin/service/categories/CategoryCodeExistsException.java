package com.cookiesstore.admin.service.categories;

public class CategoryCodeExistsException extends CategoryDomainException {

    public CategoryCodeExistsException(String code) {
        super("admin.categories.error.code.exists");
    }
}
