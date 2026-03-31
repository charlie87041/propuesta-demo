package com.cookiesstore.admin.web.dto.customers;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCustomerForm (

    @NotBlank @Size(max = 128) String name,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String password,
    @NotBlank @Size(max = 32) String phone,
    boolean active
) {
}
