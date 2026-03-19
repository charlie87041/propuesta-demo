package com.cookiesstore.admin.web.dto.users;

import jakarta.validation.constraints.NotNull;

public record AssignAbilityRequest(@NotNull Long abilityId) {
}
