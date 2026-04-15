package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.Order;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIncrementId(String incrementId);
}

