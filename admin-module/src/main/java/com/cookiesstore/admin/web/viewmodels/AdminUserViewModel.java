package com.cookiesstore.admin.web.viewmodels;


import com.cookiesstore.admin.domain.AdminUser;

public record AdminUserViewModel(Long id, String email, String roleCode) {

    public static AdminUserViewModel from(AdminUser user, String roleCode) {
        return new AdminUserViewModel(user.getId(), user.getEmail(), roleCode);
    }
}
