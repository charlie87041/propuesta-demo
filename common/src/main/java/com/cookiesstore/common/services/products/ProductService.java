package com.cookiesstore.common.services.products;

import com.cookiesstore.common.entities.Product;

public interface ProductService<C, U> {

    Product getProduct(Long productId);

    Product createProduct(C form);

    U buildUpdateProductForm(Long productId);

    Product updateProduct(Long productId, U form);

    void deleteProduct(Long productId);

    void enableProduct(Long productId);

    void deactivateProduct(Long productId);
}
