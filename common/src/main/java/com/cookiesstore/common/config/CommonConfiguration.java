package com.cookiesstore.common.config;

import com.cookiesstore.common.auth.JwtProperties;
import com.cookiesstore.common.storage.MinioProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Configuration;

@Configuration
@Import(I18nConfig.class)
@EnableConfigurationProperties({JwtProperties.class, MinioProperties.class})
public class CommonConfiguration {
}
