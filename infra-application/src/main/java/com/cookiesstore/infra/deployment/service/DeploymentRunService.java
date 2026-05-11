package com.cookiesstore.infra.deployment.service;

import com.cookiesstore.infra.catalog.domain.Project;
import com.cookiesstore.infra.catalog.domain.ProjectApplication;
import com.cookiesstore.infra.catalog.domain.ProjectEnvironment;
import com.cookiesstore.infra.catalog.service.ProjectApplicationService;
import com.cookiesstore.infra.catalog.service.ProjectEnvironmentService;
import com.cookiesstore.infra.catalog.service.ProjectService;
import com.cookiesstore.infra.deployment.domain.DeploymentOperation;
import com.cookiesstore.infra.deployment.domain.DeploymentRun;
import com.cookiesstore.infra.deployment.repository.DeploymentRunRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DeploymentRunService {

    private final ProjectService projectService;
    private final ProjectEnvironmentService environmentService;
    private final ProjectApplicationService applicationService;
    private final DeploymentRunRepository repository;

    public DeploymentRunService(
        ProjectService projectService,
        ProjectEnvironmentService environmentService,
        ProjectApplicationService applicationService,
        DeploymentRunRepository repository
    ) {
        this.projectService = projectService;
        this.environmentService = environmentService;
        this.applicationService = applicationService;
        this.repository = repository;
    }

    @Transactional
    public DeploymentRun create(CreateDeploymentRunCommand command) {
        Project project = projectService.requireMutableById(command.projectId());
        ProjectEnvironment environment = environmentService.requireById(command.environmentId());
        ProjectApplication application = command.applicationId() == null || command.applicationId().isBlank()
            ? null
            : applicationService.requireById(command.applicationId());

        if (!environment.getProject().getId().equals(project.getId())) {
            throw new IllegalArgumentException("The environment does not belong to the selected project");
        }
        environmentService.assertActive(environment);
        if (application != null && !application.getProject().getId().equals(project.getId())) {
            throw new IllegalArgumentException("The application does not belong to the selected project");
        }

        DeploymentRun run = new DeploymentRun(project, environment, application, command.operation(), command.triggeredBy());
        return repository.save(run);
    }

    public List<DeploymentRun> listByProject(String projectId) {
        return repository.findByProjectIdOrderByCreatedAtDesc(projectId);
    }

    public record CreateDeploymentRunCommand(
        String projectId,
        String environmentId,
        String applicationId,
        DeploymentOperation operation,
        String triggeredBy
    ) {
    }
}
