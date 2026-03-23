package com.cookiesstore.common.services;

import java.sql.Date;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

    @Transactional(readOnly = true)
    public Page<Customer> listCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Customer> listCustomers(Pageable pageable, String searchQuery) {
        if (!StringUtils.hasText(searchQuery)) {
            return customerRepository.findAll(pageable);
        }
        String normalizedQuery = searchQuery.trim();
        return customerRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            normalizedQuery,
            normalizedQuery,
            pageable
        );
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

    @Transactional(readOnly = true)
    public CustomerStatistics getCustomerStatistics(Date fromDate, Date toDate) {
        Instant fromInstant = fromDate.toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant toInstant = toDate.toLocalDate().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).minusNanos(1);

        Integer totalCustomers = Math.toIntExact(customerRepository.count());
        Integer newCustomersInMonth = Math.toIntExact(customerRepository.countByCreatedAtBetween(fromInstant, toInstant));
        Integer activeSessions = Math.toIntExact(customerRepository.countByActiveTrueAndCreatedAtBetween(fromInstant, toInstant));
        Float activeSessionsPercent = totalCustomers != 0 ? (activeSessions.floatValue() / totalCustomers.floatValue()) * 100 : 0;
        Float newCustomersInMonthPercent = totalCustomers != 0 ? (newCustomersInMonth.floatValue() / totalCustomers.floatValue()) * 100 : 0;
        return new CustomerStatistics(totalCustomers, newCustomersInMonth, newCustomersInMonthPercent, activeSessions, activeSessionsPercent, fromDate, toDate);
    }

}
