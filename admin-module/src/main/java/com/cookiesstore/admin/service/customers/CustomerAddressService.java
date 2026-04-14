package com.cookiesstore.admin.service.customers;

import com.cookiesstore.admin.web.dto.customers.CustomerAddressForm;
import com.cookiesstore.common.entities.Customer;
import com.cookiesstore.common.entities.CustomerAddress;
import com.cookiesstore.common.entities.CustomerAddress.AddressType;
import com.cookiesstore.common.repositories.CustomerAddressRepository;
import com.cookiesstore.common.services.CustomerService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class CustomerAddressService {

    private final CustomerService customerService;
    private final CustomerAddressRepository customerAddressRepository;

    public CustomerAddressService(
        CustomerService customerService,
        CustomerAddressRepository customerAddressRepository
    ) {
        this.customerService = customerService;
        this.customerAddressRepository = customerAddressRepository;
    }

    @Transactional(readOnly = true)
    public List<CustomerAddress> findLatestByCustomerId(Long customerId) {
        return customerAddressRepository.findByCustomerIdAndLatestTrueOrderByIdDesc(customerId);
    }

    public CustomerAddress createAddress(Long customerId, CustomerAddressForm form) {
        Customer customer = customerService.findByIdOrThrow(customerId);

        CustomerAddress address = new CustomerAddress();
        address.setCustomer(customer);
        address.setVersion(1);
        address.setLatest(true);
        address.setPreviousVersion(null);
        applyForm(address, form);

        if (address.isDefaultAddress()) {
            clearCurrentDefault(customerId, address.getAddressType());
        }

        return customerAddressRepository.save(address);
    }

    public CustomerAddress updateAddress(Long customerId, Long addressId, CustomerAddressForm form) {
        customerService.findByIdOrThrow(customerId);

        CustomerAddress current = customerAddressRepository.findByIdAndCustomerId(addressId, customerId)
            .orElseThrow(() -> new CustomerAddressNotFoundException(addressId));

        if (!current.isLatest()) {
            throw new CustomerAddressVersionConflictException(addressId);
        }

        // If there is no real change, keep current version untouched.
        if (buildContentHash(current).equals(buildContentHash(form))) {
            return current;
        }

        current.setLatest(false);
        customerAddressRepository.save(current);

        CustomerAddress next = new CustomerAddress();
        next.setCustomer(current.getCustomer());
        next.setVersion(current.getVersion() + 1);
        next.setLatest(true);
        next.setPreviousVersion(current);
        applyForm(next, form);

        if (next.isDefaultAddress()) {
            clearCurrentDefault(customerId, next.getAddressType());
        }

        return customerAddressRepository.save(next);
    }

    public void deleteAddress(Long customerId, Long addressId) {
        customerService.findByIdOrThrow(customerId);

        CustomerAddress current = customerAddressRepository.findByIdAndCustomerId(addressId, customerId)
            .orElseThrow(() -> new CustomerAddressNotFoundException(addressId));

        if (!current.isLatest()) {
            throw new CustomerAddressVersionConflictException(addressId);
        }

        current.setLatest(false);
        customerAddressRepository.save(current);
    }

    private void clearCurrentDefault(Long customerId, AddressType addressType) {
        List<CustomerAddress> defaults = customerAddressRepository
            .findByCustomerIdAndAddressTypeAndLatestTrueAndDefaultAddressTrue(customerId, addressType);
        if (defaults.isEmpty()) {
            return;
        }
        defaults.forEach(existing -> existing.setDefaultAddress(false));
        customerAddressRepository.saveAll(defaults);
    }

    private void applyForm(CustomerAddress address, CustomerAddressForm form) {
        address.setFirstName(form.firstName().trim());
        address.setLastName(form.lastName().trim());
        address.setGender(trimToNull(form.gender()));
        address.setCompanyName(trimToNull(form.companyName()));
        address.setAddress1(form.address1().trim());
        address.setAddress2(trimToNull(form.address2()));
        address.setCity(form.city().trim());
        address.setState(trimToNull(form.state()));
        address.setCountry(trimToNull(form.country()));
        address.setPostcode(trimToNull(form.postcode()));
        address.setEmail(trimToNull(form.email()));
        address.setPhone(trimToNull(form.phone()));
        address.setVatId(trimToNull(form.vatId()));
        address.setAddressType(form.addressType() == null ? AddressType.SHIPPING : form.addressType());
        address.setDefaultAddress(form.defaultAddress());
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String buildContentHash(CustomerAddress current) {
        return sha256(
            normalize(current.getFirstName()),
            normalize(current.getLastName()),
            normalize(current.getGender()),
            normalize(current.getCompanyName()),
            normalize(current.getAddress1()),
            normalize(current.getAddress2()),
            normalize(current.getCity()),
            normalize(current.getState()),
            normalize(current.getCountry()),
            normalize(current.getPostcode()),
            normalize(current.getEmail()),
            normalize(current.getPhone()),
            normalize(current.getVatId()),
            current.getAddressType() == null ? AddressType.SHIPPING.name() : current.getAddressType().name(),
            Boolean.toString(current.isDefaultAddress())
        );
    }

    private String buildContentHash(CustomerAddressForm form) {
        AddressType type = form.addressType() == null ? AddressType.SHIPPING : form.addressType();
        return sha256(
            normalize(form.firstName()),
            normalize(form.lastName()),
            normalize(form.gender()),
            normalize(form.companyName()),
            normalize(form.address1()),
            normalize(form.address2()),
            normalize(form.city()),
            normalize(form.state()),
            normalize(form.country()),
            normalize(form.postcode()),
            normalize(form.email()),
            normalize(form.phone()),
            normalize(form.vatId()),
            type.name(),
            Boolean.toString(form.defaultAddress())
        );
    }

    private String normalize(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? "" : trimmed;
    }

    private String sha256(String... parts) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (String part : parts) {
                digest.update(part.getBytes(StandardCharsets.UTF_8));
                digest.update((byte) 31); // non-printable delimiter to avoid collisions by concatenation
            }
            byte[] hash = digest.digest();
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is not available", ex);
        }
    }
}
