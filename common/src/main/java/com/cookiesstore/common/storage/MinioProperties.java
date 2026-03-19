package com.cookiesstore.common.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage.minio")
public class MinioProperties {

    private String endpoint = "http://minio:9000";
    private String accessKey = "minioadmin";
    private String secretKey = "minioadmin";
    private String customerAvatarsBucket = "customer-avatars";

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getCustomerAvatarsBucket() {
        return customerAvatarsBucket;
    }

    public void setCustomerAvatarsBucket(String customerAvatarsBucket) {
        this.customerAvatarsBucket = customerAvatarsBucket;
    }
}
