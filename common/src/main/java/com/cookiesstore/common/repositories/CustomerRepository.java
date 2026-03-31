package com.cookiesstore.common.repositories;


import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.cookiesstore.common.entities.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    public List<Customer> findByEmail(String email);

    public boolean existsByEmail(String email);

    public long countByCreatedAtBetween(Instant fromDate, Instant toDate);

    public long countByActiveTrueAndCreatedAtBetween(Instant fromDate, Instant toDate);

    public Page<Customer> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        String name,
        String email,
        Pageable pageable
    );

} 
