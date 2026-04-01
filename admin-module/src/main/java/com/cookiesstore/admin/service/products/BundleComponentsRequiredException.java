package com.cookiesstore.admin.service.products;

public class BundleComponentsRequiredException extends ProductDomainException {

    public BundleComponentsRequiredException() {
        super("admin.products.bundle.components.required");
    }
}
