package com.cookiesstore.admin.web.dto.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAdminUserForm(
    @NotBlank @Email String email,
    @Size(min = 8) String password,
    @NotBlank String roleCode
) {
}
