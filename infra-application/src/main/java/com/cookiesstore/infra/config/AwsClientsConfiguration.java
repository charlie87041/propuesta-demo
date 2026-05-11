package com.cookiesstore.infra.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.Ec2ClientBuilder;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.StsClientBuilder;

@Configuration
@EnableConfigurationProperties(AwsInfraProperties.class)
public class AwsClientsConfiguration {

    @Bean
    Ec2Client ec2Client(AwsInfraProperties properties) {
        Ec2ClientBuilder builder = Ec2Client.builder()
            .region(Region.of(properties.region()));
        if (properties.endpoint() != null) {
            builder.endpointOverride(properties.endpoint());
        }
        return builder.build();
    }

    @Bean
    StsClient stsClient(AwsInfraProperties properties) {
        StsClientBuilder builder = StsClient.builder()
            .region(Region.of(properties.region()));
        if (properties.endpoint() != null) {
            builder.endpointOverride(properties.endpoint());
        }
        return builder.build();
    }
}
