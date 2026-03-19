package com.cookiesstore.common.repositories;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cookiesstore.common.entities.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    public List<Customer> findByEmail(String email);

    public boolean existsByEmail(String email);

    
}