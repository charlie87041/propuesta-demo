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
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

@Repository
public interface ProductSourceRepository extends JpaRepository<ProductSource, Long>, JpaSpecificationExecutor<ProductSource>  {

    Optional<ProductSource> findByProductIdAndSourceId(Long productId, Long sourceId);
    List<ProductSource> findBySourceIdAndProductIdIn(Long sourceId, List<Long> productIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select ps
        from ProductSource ps
        where ps.product.id = :productId and ps.source.id = :sourceId
    """)
    Optional<ProductSource> findForUpdateByProductIdAndSourceId(@Param("productId") Long productId, @Param("sourceId") Long sourceId);

    @Modifying
    @Query("""
        update ProductSource ps
        set ps.totalSold = ps.totalSold + :quantity
        where ps.product.id = :productId and ps.source.id = :sourceId
    """)
    int incrementTotalSold(
        @Param("productId") Long productId,
        @Param("sourceId") Long sourceId,
        @Param("quantity") long quantity
    );

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
