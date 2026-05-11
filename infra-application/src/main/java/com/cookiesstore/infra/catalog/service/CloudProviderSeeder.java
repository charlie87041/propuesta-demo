package com.cookiesstore.infra.catalog.service;

import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CloudProviderSeeder implements ApplicationRunner {

    private final CloudProviderService cloudProviderService;

    public CloudProviderSeeder(CloudProviderService cloudProviderService) {
        this.cloudProviderService = cloudProviderService;
    }

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {
        cloudProviderService.createIfMissing(
            "aws",
            "Amazon Web Services",
            "Default provider for infrastructure provisioning"
        );
    }
}
