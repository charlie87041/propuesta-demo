package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.OrderAddress;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderAddressRepository extends JpaRepository<OrderAddress, Long> {

    List<OrderAddress> findByOrderId(Long orderId);
}

