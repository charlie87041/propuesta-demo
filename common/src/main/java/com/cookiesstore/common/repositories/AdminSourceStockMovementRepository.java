package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.AdminSourceStockMovement;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminSourceStockMovementRepository extends JpaRepository<AdminSourceStockMovement, Long>, JpaSpecificationExecutor<AdminSourceStockMovement>
{
    public AdminSourceStockMovement findByOrderIdAndProductIdAndSourceId(Long orderId, Long productId, Long sourceId);

    public List<AdminSourceStockMovement> findAllBySourceId(Long sourceId, Pageable page, Specification<AdminSourceStockMovement> specification );
    public List<AdminSourceStockMovement> findAllBySourceId(Long sourceId, Pageable page);

}
