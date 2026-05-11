package com.cookiesstore.infra.catalog.service;

import com.cookiesstore.infra.catalog.domain.CloudProvider;
import com.cookiesstore.infra.catalog.domain.Project;
import com.cookiesstore.infra.catalog.domain.ProjectCredential;
import com.cookiesstore.infra.catalog.repository.ProjectCredentialRepository;
import com.cookiesstore.infra.config.InfraEncryptionProperties;
import com.cookiesstore.infra.shared.infrastructure.PayloadEncryptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(InfraEncryptionProperties.class)
public class ProjectCredentialService {

    private final ProjectCredentialRepository projectCredentialRepository;
    private final PayloadEncryptionService payloadEncryptionService;
    private final InfraEncryptionProperties encryptionProperties;
    private final ObjectMapper objectMapper;

    public ProjectCredentialService(
        ProjectCredentialRepository projectCredentialRepository,
        PayloadEncryptionService payloadEncryptionService,
        InfraEncryptionProperties encryptionProperties,
        ObjectMapper objectMapper
    ) {
        this.projectCredentialRepository = projectCredentialRepository;
        this.payloadEncryptionService = payloadEncryptionService;
        this.encryptionProperties = encryptionProperties;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ProjectCredential create(Project project, CloudProvider provider, String credentialsJson) {
        if (credentialsJson == null || credentialsJson.isBlank()) {
            throw new IllegalArgumentException("Provider credentials are required");
        }
        validateJson(credentialsJson);
        String encryptedPayload = payloadEncryptionService.encrypt(credentialsJson);
        String keyVersion = encryptionProperties.keyVersion() == null || encryptionProperties.keyVersion().isBlank()
            ? "v1"
            : encryptionProperties.keyVersion().trim();

        ProjectCredential credential = new ProjectCredential(
            project,
            provider,
            provider.getCode() + "-access",
            encryptedPayload,
            keyVersion
        );
        return projectCredentialRepository.save(credential);
    }

    private void validateJson(String credentialsJson) {
        try {
            objectMapper.readTree(credentialsJson);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Provider credentials must be valid JSON");
        }
    }
}
