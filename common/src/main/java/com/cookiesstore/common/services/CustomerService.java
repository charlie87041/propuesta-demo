package com.cookiesstore.common.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cookiesstore.common.entities.Customer;
import com.cookiesstore.common.repositories.CustomerRepository;



@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public List<Customer> listCustomers() {
        return customerRepository.findAll();
    }

    public boolean disableCustomer(Long customerId) {
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        if (optionalCustomer.isEmpty()) {
            return false;
        }
        Customer customer = optionalCustomer.get();
        customer.setActive(false);
        customerRepository.save(customer);
        return true;
    }

    public boolean enableCustomer(Long customerId) {
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        if (optionalCustomer.isEmpty()) {
            return false;
        }
        Customer customer = optionalCustomer.get();
        customer.setActive(true);
        customerRepository.save(customer);
        return true;
    }

    public Customer createCustomer(
        String name,
        String email,
        String password,
        String phone,
        boolean active
    ) {
        if (customerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Customer email already exists");
        }

        Customer customer = new Customer();
        customer.setName(name);
        customer.setEmail(email);
        customer.setPassword(password);
        customer.setPhone(phone);
        customer.setActive(active);
        return customerRepository.save(customer);
    }

    public boolean updateCustomer(
        Long customerId,
        String name,
        String email,
        String phone,
        boolean active
    ) {
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        if (optionalCustomer.isEmpty()) {
            return false;
        }

        Customer customer = optionalCustomer.get();
        if (!customer.getEmail().equalsIgnoreCase(email) && customerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Customer email already exists");
        }

        customer.setName(name);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setActive(active);
        customerRepository.save(customer);
        return true;
    }

    public void updateLogoUrl(Long customerId, String logoUrl) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        customer.setLogoUrl(logoUrl);
        customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public Optional<Customer> findById(Long customerId) {
        return customerRepository.findById(customerId);
    }
}
