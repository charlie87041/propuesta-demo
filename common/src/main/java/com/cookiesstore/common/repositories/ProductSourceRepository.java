package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.ProductSource;
import com.cookiesstore.common.entities.ProductSourceStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSourceRepository extends JpaRepository<ProductSource, Long>, JpaSpecificationExecutor<ProductSource>  {

    Optional<ProductSource> findByProductIdAndSourceId(Long productId, Long sourceId);

    List<ProductSource> findBySourceIdAndStatus(Long sourceId, ProductSourceStatus status);

    void deleteByProductId(Long productId);
}
