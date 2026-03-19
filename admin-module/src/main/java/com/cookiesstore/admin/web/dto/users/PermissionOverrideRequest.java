package com.cookiesstore.admin.web.dto.users;

import jakarta.validation.constraints.NotBlank;

public record PermissionOverrideRequest(
    @NotBlank String permissionCode,
    boolean granted
) {
}
