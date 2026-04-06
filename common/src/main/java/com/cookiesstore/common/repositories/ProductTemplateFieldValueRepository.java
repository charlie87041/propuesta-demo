package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.ProductTemplateFieldValue;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductTemplateFieldValueRepository extends JpaRepository<ProductTemplateFieldValue, Long> {

    @EntityGraph(attributePaths = {"templateField"})
    List<ProductTemplateFieldValue> findByProductId(Long productId);

    void deleteByProductId(Long productId);
}
