package com.cookiesstore.admin.web.dto;

import com.cookiesstore.admin.domain.AdminUser;

public record AdminUserResponse(Long id, String email, boolean active) {

    public static AdminUserResponse from(AdminUser adminUser) {
        return new AdminUserResponse(adminUser.getId(), adminUser.getEmail(), adminUser.isActive());
    }
}
