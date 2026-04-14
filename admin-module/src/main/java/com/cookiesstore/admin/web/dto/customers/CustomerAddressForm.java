package com.cookiesstore.admin.web.dto.customers;

import com.cookiesstore.common.entities.CustomerAddress.AddressType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerAddressForm(
    @NotBlank @Size(max = 120) String firstName,
    @NotBlank @Size(max = 120) String lastName,
    @Size(max = 40) String gender,
    @Size(max = 180) String companyName,
    @NotBlank @Size(max = 255) String address1,
    @Size(max = 255) String address2,
    @NotBlank @Size(max = 140) String city,
    @Size(max = 140) String state,
    @Size(max = 140) String country,
    @Size(max = 32) String postcode,
    @Email @Size(max = 255) String email,
    @Size(max = 32) String phone,
    @Size(max = 64) String vatId,
    AddressType addressType,
    boolean defaultAddress
) {
}

