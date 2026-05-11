package com.cookiesstore.infra.catalog.domain;

import com.cookiesstore.infra.shared.domain.AbstractAuditableEntity;
import com.cookiesstore.infra.shared.domain.RecordStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "project_credentials")
public class ProjectCredential extends AbstractAuditableEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private CloudProvider provider;

    @Column(name = "credential_type", nullable = false, length = 50)
    private String credentialType;

    @Column(name = "encrypted_payload", nullable = false, columnDefinition = "TEXT")
    private String encryptedPayload;

    @Column(name = "key_version", nullable = false, length = 20)
    private String keyVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordStatus status;

    protected ProjectCredential() {
    }

    public ProjectCredential(
        Project project,
        CloudProvider provider,
        String credentialType,
        String encryptedPayload,
        String keyVersion
    ) {
        this.project = project;
        this.provider = provider;
        this.credentialType = credentialType;
        this.encryptedPayload = encryptedPayload;
        this.keyVersion = keyVersion;
        this.status = RecordStatus.ACTIVE;
    }

    public Project getProject() {
        return project;
    }

    public CloudProvider getProvider() {
        return provider;
    }

    public String getCredentialType() {
        return credentialType;
    }

    public String getEncryptedPayload() {
        return encryptedPayload;
    }

    public String getKeyVersion() {
        return keyVersion;
    }

    public RecordStatus getStatus() {
        return status;
    }
}
