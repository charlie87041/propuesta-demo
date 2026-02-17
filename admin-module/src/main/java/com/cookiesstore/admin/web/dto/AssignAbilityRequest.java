package com.cookiesstore.admin.web.dto;

import jakarta.validation.constraints.NotNull;

public record AssignAbilityRequest(@NotNull Long abilityId) {
}
