package com.cookiesstore.infra.config;

import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "infra.aws")
public record AwsInfraProperties(
    @NotBlank String region,
    URI endpoint
) {
}
