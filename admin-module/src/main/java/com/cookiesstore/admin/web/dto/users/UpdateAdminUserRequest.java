package com.cookiesstore.admin.web.dto.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateAdminUserRequest(
    @Email String email,
    String password,
    @NotBlank String roleCode
) {
}
