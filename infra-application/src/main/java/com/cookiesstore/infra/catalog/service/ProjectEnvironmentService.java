package com.cookiesstore.infra.catalog.service;

import com.cookiesstore.infra.catalog.domain.Project;
import com.cookiesstore.infra.catalog.domain.ProjectEnvironment;
import com.cookiesstore.infra.catalog.repository.ProjectEnvironmentRepository;
import com.cookiesstore.infra.deployment.repository.DeploymentDefinitionRepository;
import com.cookiesstore.infra.deployment.repository.DeploymentRunRepository;
import com.cookiesstore.infra.shared.domain.RecordStatus;
import com.cookiesstore.infra.topology.repository.ApplicationResourceBindingRepository;
import com.cookiesstore.infra.topology.repository.ApplicationServiceDefinitionRepository;
import com.cookiesstore.infra.topology.repository.ProjectResourceDefinitionRepository;
import jakarta.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProjectEnvironmentService {

    private final ProjectService projectService;
    private final ProjectEnvironmentRepository environmentRepository;
    private final ProjectResourceDefinitionRepository resourceDefinitionRepository;
    private final ApplicationServiceDefinitionRepository serviceDefinitionRepository;
    private final ApplicationResourceBindingRepository bindingRepository;
    private final DeploymentDefinitionRepository deploymentDefinitionRepository;
    private final DeploymentRunRepository deploymentRunRepository;

    public ProjectEnvironmentService(
        ProjectService projectService,
        ProjectEnvironmentRepository environmentRepository,
        ProjectResourceDefinitionRepository resourceDefinitionRepository,
        ApplicationServiceDefinitionRepository serviceDefinitionRepository,
        ApplicationResourceBindingRepository bindingRepository,
        DeploymentDefinitionRepository deploymentDefinitionRepository,
        DeploymentRunRepository deploymentRunRepository
    ) {
        this.projectService = projectService;
        this.environmentRepository = environmentRepository;
        this.resourceDefinitionRepository = resourceDefinitionRepository;
        this.serviceDefinitionRepository = serviceDefinitionRepository;
        this.bindingRepository = bindingRepository;
        this.deploymentDefinitionRepository = deploymentDefinitionRepository;
        this.deploymentRunRepository = deploymentRunRepository;
    }

    @Transactional
    public ProjectEnvironment create(CreateEnvironmentCommand command) {
        Project project = projectService.requireMutableById(command.projectId());
        environmentRepository.findByProjectIdAndNameIgnoreCase(project.getId(), command.name())
            .ifPresent(environment -> {
                throw new IllegalArgumentException("An environment with that name already exists in the project");
            });

        String region = command.region() == null || command.region().isBlank()
            ? project.getDefaultRegion()
            : command.region().trim();

        ProjectEnvironment environment = new ProjectEnvironment(project, command.name().trim(), region, command.domain());
        return environmentRepository.save(environment);
    }

    @Transactional
    public ProjectEnvironment update(String projectId, String environmentId, UpdateEnvironmentCommand command) {
        ProjectEnvironment environment = requireByProjectId(projectId, environmentId);
        projectService.assertMutable(environment.getProject());
        environmentRepository.findByProjectIdAndNameIgnoreCase(projectId, command.name().trim())
            .ifPresent(existing -> {
                if (!existing.getId().equals(environment.getId())) {
                    throw new IllegalArgumentException("An environment with that name already exists in the project");
                }
            });

        String region = command.region() == null || command.region().isBlank()
            ? environment.getProject().getDefaultRegion()
            : command.region().trim();
        environment.updateDetails(command.name().trim(), region, command.domain());
        return environment;
    }

    @Transactional
    public void archive(String projectId, String environmentId) {
        ProjectEnvironment environment = requireByProjectId(projectId, environmentId);
        projectService.assertMutable(environment.getProject());
        environment.archive();
    }

    @Transactional
    public void activate(String projectId, String environmentId) {
        ProjectEnvironment environment = requireByProjectId(projectId, environmentId);
        projectService.assertMutable(environment.getProject());
        environment.activate();
    }

    @Transactional
    public void deleteIfUnused(String projectId, String environmentId) {
        ProjectEnvironment environment = requireByProjectId(projectId, environmentId);
        projectService.assertMutable(environment.getProject());
        if (!canDelete(environment.getId())) {
            throw new IllegalArgumentException("Only environments without topology or deployments can be deleted");
        }
        environmentRepository.delete(environment);
    }

    public List<ProjectEnvironment> listActiveEnvironments() {
        return environmentRepository.findAll().stream()
            .filter(e -> e.getStatus() == RecordStatus.ACTIVE)
            .sorted(Comparator.comparing((ProjectEnvironment e) -> e.getProject().getName())
                .thenComparing(ProjectEnvironment::getName))
            .toList();
    }

    public List<ProjectEnvironment> listByProject(String projectId) {
        return environmentRepository.findByProjectIdOrderByNameAsc(projectId);
    }

    public List<ProjectEnvironmentListItem> listItemsByProject(String projectId) {
        return environmentRepository.findByProjectIdOrderByNameAsc(projectId)
            .stream()
            .map(environment -> new ProjectEnvironmentListItem(environment, canDelete(environment.getId())))
            .toList();
    }

    public ProjectEnvironment requireById(String environmentId) {
        return environmentRepository.findById(environmentId)
            .orElseThrow(() -> new IllegalArgumentException("Environment not found"));
    }

    public ProjectEnvironment requireByProjectId(String projectId, String environmentId) {
        ProjectEnvironment environment = requireById(environmentId);
        if (!environment.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Environment not found in the selected project");
        }
        return environment;
    }

    public void assertActive(ProjectEnvironment environment) {
        if (environment.getStatus() != RecordStatus.ACTIVE) {
            throw new IllegalArgumentException("Archived environments cannot receive topology or deployments");
        }
    }

    public boolean canDelete(String environmentId) {
        return !resourceDefinitionRepository.existsByEnvironmentId(environmentId)
            && !serviceDefinitionRepository.existsByEnvironmentId(environmentId)
            && !bindingRepository.existsByEnvironmentId(environmentId)
            && !deploymentDefinitionRepository.existsByEnvironmentId(environmentId)
            && !deploymentRunRepository.existsByEnvironmentId(environmentId);
    }

    public record CreateEnvironmentCommand(
        String projectId,
        String name,
        String region,
        String domain
    ) {
    }

    public record UpdateEnvironmentCommand(
        String name,
        String region,
        String domain
    ) {
    }

    public record ProjectEnvironmentListItem(
        ProjectEnvironment environment,
        boolean deletable
    ) {
        public boolean active() {
            return environment.getStatus() == RecordStatus.ACTIVE;
        }
    }
}
