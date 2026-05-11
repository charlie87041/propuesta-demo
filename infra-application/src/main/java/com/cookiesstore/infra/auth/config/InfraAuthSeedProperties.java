package com.cookiesstore.infra.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "infra.auth.seed")
public record InfraAuthSeedProperties(
    String username,
    String password,
    String displayName
) {
}
