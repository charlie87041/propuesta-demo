package com.cookiesstore.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.cookiesstore.common.entities.ProductTemplate;
import java.util.Optional;

@Repository
public interface ProductTemplateRepository extends JpaRepository<ProductTemplate, Long>, JpaSpecificationExecutor<ProductTemplate> {

    Optional<ProductTemplate> findByCode(String code);
}
