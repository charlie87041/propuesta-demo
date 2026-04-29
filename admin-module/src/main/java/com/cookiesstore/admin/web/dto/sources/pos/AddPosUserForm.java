package com.cookiesstore.admin.web.dto.sources.pos;

import com.cookiesstore.admin.domain.pos.PosOperatorRole;
import jakarta.validation.constraints.NotNull;

public class AddPosUserForm {

    @NotNull
    private Long adminUserId;

    @NotNull
    private PosOperatorRole role = PosOperatorRole.POS_CASHIER;

    public Long getAdminUserId() {
        return adminUserId;
    }

    public void setAdminUserId(Long adminUserId) {
        this.adminUserId = adminUserId;
    }

    public PosOperatorRole getRole() {
        return role;
    }

    public void setRole(PosOperatorRole role) {
        this.role = role;
    }
}
