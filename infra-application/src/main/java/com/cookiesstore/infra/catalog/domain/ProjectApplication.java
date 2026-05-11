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
@Table(name = "project_applications")
public class ProjectApplication extends AbstractAuditableEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "application_key", nullable = false, length = 100)
    private String key;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "build_type", nullable = false, length = 30)
    private ApplicationBuildType buildType;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 30)
    private ApplicationServiceType serviceType;

    @Column(length = 100)
    private String runtime;

    @Column(name = "source_location", length = 255)
    private String sourceLocation;

    @Column(name = "default_port")
    private Integer defaultPort;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordStatus status;

    protected ProjectApplication() {
    }

    public ProjectApplication(
        Project project,
        String key,
        String name,
        String description,
        ApplicationBuildType buildType,
        ApplicationServiceType serviceType,
        String runtime,
        String sourceLocation,
        Integer defaultPort
    ) {
        this.project = project;
        this.key = key;
        this.name = name;
        this.description = description;
        this.buildType = buildType;
        this.serviceType = serviceType;
        this.runtime = runtime;
        this.sourceLocation = sourceLocation;
        this.defaultPort = defaultPort;
        this.status = RecordStatus.ACTIVE;
    }

    public void updateDetails(
        String key,
        String name,
        String description,
        ApplicationBuildType buildType,
        ApplicationServiceType serviceType,
        String runtime,
        String sourceLocation,
        Integer defaultPort
    ) {
        this.key = key;
        this.name = name;
        this.description = description;
        this.buildType = buildType;
        this.serviceType = serviceType;
        this.runtime = runtime;
        this.sourceLocation = sourceLocation;
        this.defaultPort = defaultPort;
    }

    public void activate() {
        this.status = RecordStatus.ACTIVE;
    }

    public void archive() {
        this.status = RecordStatus.ARCHIVED;
    }

    public Project getProject() {
        return project;
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

    public ApplicationBuildType getBuildType() {
        return buildType;
    }

    public ApplicationServiceType getServiceType() {
        return serviceType;
    }

    public String getRuntime() {
        return runtime;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public Integer getDefaultPort() {
        return defaultPort;
    }

    public RecordStatus getStatus() {
        return status;
    }
}
