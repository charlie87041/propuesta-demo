package com.cookiesstore.admin.web.dto;

import jakarta.validation.constraints.Email;

public record UpdateAdminUserRequest(
    @Email String email,
    String password
) {
}
