package com.cookiesstore.infra.topology.domain;

import com.cookiesstore.infra.catalog.domain.ApplicationServiceType;
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
@Table(name = "application_service_definitions")
public class ApplicationServiceDefinition extends AbstractAuditableEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private ProjectApplication application;

    @ManyToOne(optional = false)
    @JoinColumn(name = "environment_id", nullable = false)
    private ProjectEnvironment environment;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 30)
    private ApplicationServiceType serviceType;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "config_json", nullable = false, columnDefinition = "TEXT")
    private String configJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordStatus status;

    protected ApplicationServiceDefinition() {
    }

    public ApplicationServiceDefinition(
        ProjectApplication application,
        ProjectEnvironment environment,
        ApplicationServiceType serviceType,
        String name,
        String configJson
    ) {
        this.application = application;
        this.environment = environment;
        this.serviceType = serviceType;
        this.name = name;
        this.configJson = configJson;
        this.status = RecordStatus.ACTIVE;
    }

    public ProjectApplication getApplication() {
        return application;
    }

    public ProjectEnvironment getEnvironment() {
        return environment;
    }

    public ApplicationServiceType getServiceType() {
        return serviceType;
    }

    public String getName() {
        return name;
    }

    public String getConfigJson() {
        return configJson;
    }

    public RecordStatus getStatus() {
        return status;
    }
}
