package com.cookiesstore.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "infra.security")
public record InfraEncryptionProperties(
    String encryptionKey,
    String keyVersion
) {
}
