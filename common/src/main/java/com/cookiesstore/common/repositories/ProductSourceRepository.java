package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.ProductSource;
import com.cookiesstore.common.entities.ProductSourceStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSourceRepository extends JpaRepository<ProductSource, Long>, JpaSpecificationExecutor<ProductSource>  {

    Optional<ProductSource> findByProductIdAndSourceId(Long productId, Long sourceId);

    List<ProductSource> findByProductId(Long productId);

    List<ProductSource> findBySourceIdAndStatus(Long sourceId, ProductSourceStatus status);

    @Override
    @EntityGraph(attributePaths = {"product", "product.category", "source"})
    Page<ProductSource> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"product", "product.category", "source"})
    Page<ProductSource> findAll(Specification<ProductSource> spec, Pageable pageable);

    void deleteByProductId(Long productId);
}
