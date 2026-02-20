package com.cookiesstore.admin.web.dto.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateAdminUserRequest(
    @NotBlank @Email String email,
    @NotBlank String password,
    @NotBlank String roleCode
) {
}
