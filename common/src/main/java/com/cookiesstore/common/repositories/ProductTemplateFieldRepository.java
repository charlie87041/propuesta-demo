package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.ProductTemplateField;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductTemplateFieldRepository extends JpaRepository<ProductTemplateField, Long> {

    List<ProductTemplateField> findByTemplateIdOrderBySortOrderAsc(Long templateId);
}
