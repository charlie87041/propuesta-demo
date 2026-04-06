package com.cookiesstore.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cookiesstore.common.entities.ProductTemplate;

@Repository
public interface ProductTemplateRepository extends JpaRepository<ProductTemplate, Long> {
}
