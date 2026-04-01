package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.ProductVariant;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long>, JpaSpecificationExecutor<ProductVariant>  {

    Optional<ProductVariant> findByIdAndParentProductId(Long id, Long parentProductId);

    @EntityGraph(attributePaths = {"parentProduct", "variantProduct"})
    @Query("select pv from ProductVariant pv where pv.id = :id and pv.parentProduct.id = :parentProductId")
    Optional<ProductVariant> findWithDetailsByIdAndParentProductId(Long id, Long parentProductId);

    Page<ProductVariant> findByParentProductId(Long parentProductId, Pageable pageable);

    Optional<ProductVariant> findByVariantProductId(Long variantProductId);

    List<ProductVariant> findByParentProductIdOrderBySortOrderAsc(Long parentProductId);

    void deleteByVariantProductId(Long variantProductId);

    void deleteByParentProductId(Long parentProductId);
}
