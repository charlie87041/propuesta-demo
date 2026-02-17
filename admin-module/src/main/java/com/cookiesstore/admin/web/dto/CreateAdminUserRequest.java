package com.cookiesstore.admin.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateAdminUserRequest(
    @NotBlank @Email String email,
    @NotBlank String password
) {
}
