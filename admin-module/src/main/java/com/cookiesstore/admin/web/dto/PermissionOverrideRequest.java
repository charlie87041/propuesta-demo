package com.cookiesstore.admin.web.dto;

import jakarta.validation.constraints.NotBlank;

public record PermissionOverrideRequest(
    @NotBlank String permissionCode,
    boolean granted
) {
}
