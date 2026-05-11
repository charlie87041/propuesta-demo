package com.cookiesstore.infra.topology.domain;

import com.cookiesstore.infra.catalog.domain.Project;
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
@Table(name = "project_resource_definitions")
public class ProjectResourceDefinition extends AbstractAuditableEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "environment_id", nullable = false)
    private ProjectEnvironment environment;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 50)
    private ResourceType resourceType;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "config_json", nullable = false, columnDefinition = "TEXT")
    private String configJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordStatus status;

    protected ProjectResourceDefinition() {
    }

    public ProjectResourceDefinition(
        Project project,
        ProjectEnvironment environment,
        ResourceType resourceType,
        String name,
        String description,
        String configJson
    ) {
        this.project = project;
        this.environment = environment;
        this.resourceType = resourceType;
        this.name = name;
        this.description = description;
        this.configJson = configJson;
        this.status = RecordStatus.ACTIVE;
    }

    public Project getProject() {
        return project;
    }

    public ProjectEnvironment getEnvironment() {
        return environment;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getConfigJson() {
        return configJson;
    }

    public RecordStatus getStatus() {
        return status;
    }

    public void updateDetails(String name, String description, String configJson) {
        this.name = name;
        this.description = description;
        this.configJson = configJson;
    }

    public void activate() {
        this.status = RecordStatus.ACTIVE;
    }

    public void archive() {
        this.status = RecordStatus.ARCHIVED;
    }
}
