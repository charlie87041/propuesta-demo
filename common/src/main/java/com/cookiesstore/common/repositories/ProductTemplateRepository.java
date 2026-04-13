package com.cookiesstore.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.cookiesstore.common.entities.ProductTemplate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Repository
public interface ProductTemplateRepository extends JpaRepository<ProductTemplate, Long>, JpaSpecificationExecutor<ProductTemplate> {

    Optional<ProductTemplate> findByCode(String code);

    boolean existsByCode(String code);

    Optional<ProductTemplate> findTopByCodeOrderByVersionDesc(String code);

    Optional<ProductTemplate> findByCodeAndLatestTrue(String code);

    Page<ProductTemplate> findByLatestTrue(Pageable pageable);

    List<ProductTemplate> findByLatestTrue(Sort sort);
}
