package com.cookiesstore.infra.catalog.domain;

import com.cookiesstore.infra.shared.domain.AbstractAuditableEntity;
import com.cookiesstore.infra.shared.domain.RecordStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "projects")
public class Project extends AbstractAuditableEntity {

    @Column(name = "project_key", nullable = false, unique = true, length = 100)
    private String key;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "default_region", nullable = false, length = 50)
    private String defaultRegion;

    @Column(length = 150)
    private String owner;

    @Column(name = "provider_code", nullable = false, length = 50)
    private String providerCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordStatus status;

    protected Project() {
    }

    public Project(String key, String name, String description, String defaultRegion, String owner, String providerCode) {
        this.key = key;
        this.name = name;
        this.description = description;
        this.defaultRegion = defaultRegion;
        this.owner = owner;
        this.providerCode = providerCode.toLowerCase();
        this.status = RecordStatus.ACTIVE;
    }

    public void updateDetails(String name, String description, String defaultRegion, String owner) {
        this.name = name;
        this.description = description;
        this.defaultRegion = defaultRegion;
        this.owner = owner;
    }

    public void activate() {
        this.status = RecordStatus.ACTIVE;
    }

    public void archive() {
        this.status = RecordStatus.ARCHIVED;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultRegion() {
        return defaultRegion;
    }

    public String getOwner() {
        return owner;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public RecordStatus getStatus() {
        return status;
    }
}
