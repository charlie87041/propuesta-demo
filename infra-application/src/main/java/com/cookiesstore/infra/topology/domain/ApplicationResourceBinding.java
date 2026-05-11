package com.cookiesstore.infra.topology.domain;

import com.cookiesstore.infra.catalog.domain.ProjectApplication;
import com.cookiesstore.infra.catalog.domain.ProjectEnvironment;
import com.cookiesstore.infra.shared.domain.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "application_resource_bindings")
public class ApplicationResourceBinding extends AbstractAuditableEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private ProjectApplication application;

    @ManyToOne(optional = false)
    @JoinColumn(name = "environment_id", nullable = false)
    private ProjectEnvironment environment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_resource_definition_id", nullable = false)
    private ProjectResourceDefinition projectResourceDefinition;

    @Enumerated(EnumType.STRING)
    @Column(name = "binding_type", nullable = false, length = 30)
    private BindingType bindingType;

    @Column(name = "mount_as", length = 100)
    private String mountAs;

    @Column(name = "config_json", nullable = false, columnDefinition = "TEXT")
    private String configJson;

    protected ApplicationResourceBinding() {
    }

    public ApplicationResourceBinding(
        ProjectApplication application,
        ProjectEnvironment environment,
        ProjectResourceDefinition projectResourceDefinition,
        BindingType bindingType,
        String mountAs,
        String configJson
    ) {
        this.application = application;
        this.environment = environment;
        this.projectResourceDefinition = projectResourceDefinition;
        this.bindingType = bindingType;
        this.mountAs = mountAs;
        this.configJson = configJson;
    }

    public ProjectApplication getApplication() {
        return application;
    }

    public ProjectEnvironment getEnvironment() {
        return environment;
    }

    public ProjectResourceDefinition getProjectResourceDefinition() {
        return projectResourceDefinition;
    }

    public BindingType getBindingType() {
        return bindingType;
    }

    public String getMountAs() {
        return mountAs;
    }

    public String getConfigJson() {
        return configJson;
    }
}
