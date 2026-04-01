package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.ProductComponent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductComponentRepository extends JpaRepository<ProductComponent, Long> {

    List<ProductComponent> findByParentProductIdOrderBySortOrderAsc(Long parentProductId);

    void deleteByParentProductId(Long parentProductId);
}
