package com.cookiesstore.pos.auth;

public record PosPrincipal(Long userId, Long sourceId, String email) {
}
