package com.cookiesstore.common.config;

import com.cookiesstore.common.storage.MinioProperties;
import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Bean
    public MinioClient minioClient(MinioProperties minioProperties) {
        return MinioClient.builder()
            .endpoint(minioProperties.getEndpoint())
            .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
            .build();
    }
}
