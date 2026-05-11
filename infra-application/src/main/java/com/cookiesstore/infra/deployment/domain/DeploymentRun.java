package com.cookiesstore.infra.deployment.domain;

import com.cookiesstore.infra.catalog.domain.Project;
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
import java.time.LocalDateTime;

@Entity
@Table(name = "deployment_runs")
public class DeploymentRun extends AbstractAuditableEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "environment_id", nullable = false)
    private ProjectEnvironment environment;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private ProjectApplication application;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeploymentOperation operation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeploymentRunStatus status;

    @Column(name = "triggered_by", length = 100)
    private String triggeredBy;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(length = 1000)
    private String summary;

    @Column(name = "logs_path", length = 255)
    private String logsPath;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    protected DeploymentRun() {
    }

    public DeploymentRun(
        Project project,
        ProjectEnvironment environment,
        ProjectApplication application,
        DeploymentOperation operation,
        String triggeredBy
    ) {
        this.project = project;
        this.environment = environment;
        this.application = application;
        this.operation = operation;
        this.triggeredBy = triggeredBy;
        this.status = DeploymentRunStatus.PENDING;
        this.startedAt = LocalDateTime.now();
    }

    public Project getProject() {
        return project;
    }

    public ProjectEnvironment getEnvironment() {
        return environment;
    }

    public ProjectApplication getApplication() {
        return application;
    }

    public DeploymentOperation getOperation() {
        return operation;
    }

    public DeploymentRunStatus getStatus() {
        return status;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public String getSummary() {
        return summary;
    }

    public String getLogsPath() {
        return logsPath;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void markRunning() {
        this.status = DeploymentRunStatus.RUNNING;
    }

    public void markSucceeded(String summary, String logsPath) {
        this.status = DeploymentRunStatus.SUCCEEDED;
        this.summary = summary;
        this.logsPath = logsPath;
        this.finishedAt = LocalDateTime.now();
    }

    public void markFailed(String errorMessage, String logsPath) {
        this.status = DeploymentRunStatus.FAILED;
        this.errorMessage = errorMessage;
        this.logsPath = logsPath;
        this.finishedAt = LocalDateTime.now();
    }
}
