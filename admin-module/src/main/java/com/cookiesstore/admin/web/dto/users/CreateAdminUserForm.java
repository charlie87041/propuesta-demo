package com.cookiesstore.admin.web.dto.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAdminUserForm(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String password,
    @NotBlank String roleCode
) {
}
