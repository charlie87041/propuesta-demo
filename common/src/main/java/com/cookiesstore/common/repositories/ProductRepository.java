package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product>  {

    interface CategoryProductCountsProjection {
        Long getCategoryId();

        Long getTotalProducts();

        Long getActiveProducts();

        Long getActiveListableProducts();
    }

    Optional<Product> findBySku(String sku);

    Optional<Product> findBySlug(String slug);

    boolean existsBySku(String sku);

    boolean existsByCategoryId(Long categoryId);

    boolean existsByTemplateId(Long templateId);

    Page<Product> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(String name, String sku, Pageable pageable);

    Optional<Product> findByIdAndProductTypeCode(Long id, String productTypeCode);

    @EntityGraph(attributePaths = {"currentPrice"})
    List<Product> findByActiveTrueAndListableTrueOrderByNameAsc();

    @EntityGraph(attributePaths = {"components", "components.childProduct", "components.source"})
    @Query("select p from Product p where p.id = :id and p.productTypeCode = :productTypeCode")
    Optional<Product> findBundleByIdAndProductTypeCode(Long id, String productTypeCode);

    @Query("select p from Product p where p.id = :id and p.productTypeCode = :productTypeCode")
    @EntityGraph(attributePaths = {"packageOptions", "packageOptions.optionType", "packageOptions.category"})
    Optional<Product> findPackageByIdAndProductTypeCode(Long id, String productTypeCode);

    @Query(
        """
        select
            p.category.id as categoryId,
            count(p.id) as totalProducts,
            sum(case when p.active = true then 1 else 0 end) as activeProducts,
            sum(case when p.active = true and p.listable = true then 1 else 0 end) as activeListableProducts
        from Product p
        where p.category.id in :categoryIds
        group by p.category.id
        """
    )
    List<CategoryProductCountsProjection> countProductsByCategoryIds(@Param("categoryIds") List<Long> categoryIds);
}
