package com.cookiesstore.common.dto.orders;

import com.cookiesstore.common.entities.OrderAddressType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderAddressForm(
    @NotNull OrderAddressType addressType,
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
    @Size(max = 64) String vatId
){}
