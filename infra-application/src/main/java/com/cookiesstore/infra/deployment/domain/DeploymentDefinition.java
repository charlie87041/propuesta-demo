package com.cookiesstore.infra.deployment.domain;

import com.cookiesstore.infra.catalog.domain.ApplicationBuildType;
import com.cookiesstore.infra.catalog.domain.ProjectApplication;
import com.cookiesstore.infra.catalog.domain.ProjectEnvironment;
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
@Table(name = "deployment_definitions")
public class DeploymentDefinition extends AbstractAuditableEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private ProjectApplication application;

    @ManyToOne(optional = false)
    @JoinColumn(name = "environment_id", nullable = false)
    private ProjectEnvironment environment;

    @Enumerated(EnumType.STRING)
    @Column(name = "build_type", nullable = false, length = 30)
    private ApplicationBuildType buildType;

    @Column(name = "artifact_source", length = 255)
    private String artifactSource;

    @Column(length = 255)
    private String entrypoint;

    @Column(name = "deploy_config_json", nullable = false, columnDefinition = "TEXT")
    private String deployConfigJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordStatus status;

    protected DeploymentDefinition() {
    }

    public DeploymentDefinition(
        ProjectApplication application,
        ProjectEnvironment environment,
        ApplicationBuildType buildType,
        String artifactSource,
        String entrypoint,
        String deployConfigJson
    ) {
        this.application = application;
        this.environment = environment;
        this.buildType = buildType;
        this.artifactSource = artifactSource;
        this.entrypoint = entrypoint;
        this.deployConfigJson = deployConfigJson;
        this.status = RecordStatus.ACTIVE;
    }

    public ProjectApplication getApplication() {
        return application;
    }

    public ProjectEnvironment getEnvironment() {
        return environment;
    }

    public ApplicationBuildType getBuildType() {
        return buildType;
    }

    public String getArtifactSource() {
        return artifactSource;
    }

    public String getEntrypoint() {
        return entrypoint;
    }

    public String getDeployConfigJson() {
        return deployConfigJson;
    }

    public RecordStatus getStatus() {
        return status;
    }
}
