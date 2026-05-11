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
@Table(name = "environments")
public class ProjectEnvironment extends AbstractAuditableEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "environment_name", nullable = false, length = 50)
    private String name;

    @Column(length = 50)
    private String region;

    @Column(length = 255)
    private String domain;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordStatus status;

    protected ProjectEnvironment() {
    }

    public ProjectEnvironment(Project project, String name, String region, String domain) {
        this.project = project;
        this.name = name;
        this.region = region;
        this.domain = domain;
        this.status = RecordStatus.ACTIVE;
    }

    public void updateDetails(String name, String region, String domain) {
        this.name = name;
        this.region = region;
        this.domain = domain;
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

    public String getName() {
        return name;
    }

    public String getRegion() {
        return region;
    }

    public String getDomain() {
        return domain;
    }

    public RecordStatus getStatus() {
        return status;
    }
}
