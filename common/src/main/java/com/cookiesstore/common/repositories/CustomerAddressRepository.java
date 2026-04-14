package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.CustomerAddress;
import com.cookiesstore.common.entities.CustomerAddress.AddressType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {

    List<CustomerAddress> findByCustomerIdAndLatestTrueOrderByIdDesc(Long customerId);

    Optional<CustomerAddress> findByIdAndCustomerId(Long id, Long customerId);

    List<CustomerAddress> findByCustomerIdAndAddressTypeAndLatestTrueAndDefaultAddressTrue(Long customerId, AddressType addressType);
}

