package com.cookiesstore.admin.web.dto.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCustomerForm (

    @NotBlank @Size(min = 8) String name,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String password,
    @NotBlank @Size(max = 128) String phone,
    @NotBlank @Size(max = 255) String logoUrl,
    boolean active
) {
}
