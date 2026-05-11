package com.cookiesstore.infra.catalog.service;

import com.cookiesstore.infra.catalog.domain.ApplicationBuildType;
import com.cookiesstore.infra.catalog.domain.ApplicationServiceType;
import com.cookiesstore.infra.catalog.domain.Project;
import com.cookiesstore.infra.catalog.domain.ProjectApplication;
import com.cookiesstore.infra.catalog.repository.ProjectApplicationRepository;
import com.cookiesstore.infra.deployment.repository.DeploymentDefinitionRepository;
import com.cookiesstore.infra.deployment.repository.DeploymentRunRepository;
import com.cookiesstore.infra.shared.domain.RecordStatus;
import com.cookiesstore.infra.topology.repository.ApplicationResourceBindingRepository;
import com.cookiesstore.infra.topology.repository.ApplicationServiceDefinitionRepository;
import jakarta.transaction.Transactional;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class ProjectApplicationService {

    private final ProjectService projectService;
    private final ProjectApplicationRepository applicationRepository;
    private final ApplicationServiceDefinitionRepository serviceDefinitionRepository;
    private final ApplicationResourceBindingRepository bindingRepository;
    private final DeploymentDefinitionRepository deploymentDefinitionRepository;
    private final DeploymentRunRepository deploymentRunRepository;

    public ProjectApplicationService(
        ProjectService projectService,
        ProjectApplicationRepository applicationRepository,
        ApplicationServiceDefinitionRepository serviceDefinitionRepository,
        ApplicationResourceBindingRepository bindingRepository,
        DeploymentDefinitionRepository deploymentDefinitionRepository,
        DeploymentRunRepository deploymentRunRepository
    ) {
        this.projectService = projectService;
        this.applicationRepository = applicationRepository;
        this.serviceDefinitionRepository = serviceDefinitionRepository;
        this.bindingRepository = bindingRepository;
        this.deploymentDefinitionRepository = deploymentDefinitionRepository;
        this.deploymentRunRepository = deploymentRunRepository;
    }

    

    @Transactional
    public ProjectApplication create(CreateApplicationCommand command) {
        validateCombination(command.buildType(), command.serviceType());
        validateSourceLocation(command.buildType(), command.sourceLocation());
        Project project = projectService.requireMutableById(command.projectId());
        String normalizedKey = normalizeKey(command.key());
        applicationRepository.findByProjectIdAndKeyIgnoreCase(project.getId(), normalizedKey)
            .ifPresent(application -> {
                throw new IllegalArgumentException("An application with that key already exists in the project");
            });
        applicationRepository.findByProjectIdAndNameIgnoreCase(project.getId(), command.name().trim())
            .ifPresent(application -> {
                throw new IllegalArgumentException("An application with that name already exists in the project");
            });

        ProjectApplication application = new ProjectApplication(
            project,
            normalizedKey,
            command.name().trim(),
            command.description(),
            command.buildType(),
            command.serviceType(),
            command.runtime(),
            command.sourceLocation().trim(),
            command.defaultPort()
        );
        return applicationRepository.save(application);
    }

    @Transactional
    public ProjectApplication update(String projectId, String applicationId, UpdateApplicationCommand command) {
        validateCombination(command.buildType(), command.serviceType());
        validateSourceLocation(command.buildType(), command.sourceLocation());
        ProjectApplication application = requireByProjectId(projectId, applicationId);
        projectService.assertMutable(application.getProject());
        String normalizedKey = normalizeKey(command.key());
        applicationRepository.findByProjectIdAndKeyIgnoreCase(projectId, normalizedKey)
            .ifPresent(existing -> {
                if (!existing.getId().equals(application.getId())) {
                    throw new IllegalArgumentException("An application with that key already exists in the project");
                }
            });
        applicationRepository.findByProjectIdAndNameIgnoreCase(projectId, command.name().trim())
            .ifPresent(existing -> {
                if (!existing.getId().equals(application.getId())) {
                    throw new IllegalArgumentException("An application with that name already exists in the project");
                }
            });

        application.updateDetails(
            normalizedKey,
            command.name().trim(),
            command.description(),
            command.buildType(),
            command.serviceType(),
            command.runtime(),
            command.sourceLocation().trim(),
            command.defaultPort()
        );
        return application;
    }

    @Transactional
    public void archive(String projectId, String applicationId) {
        ProjectApplication application = requireByProjectId(projectId, applicationId);
        projectService.assertMutable(application.getProject());
        application.archive();
    }

    @Transactional
    public void activate(String projectId, String applicationId) {
        ProjectApplication application = requireByProjectId(projectId, applicationId);
        projectService.assertMutable(application.getProject());
        application.activate();
    }

    @Transactional
    public void deleteIfUnused(String projectId, String applicationId) {
        ProjectApplication application = requireByProjectId(projectId, applicationId);
        projectService.assertMutable(application.getProject());
        if (!canDelete(application.getId())) {
            throw new IllegalArgumentException("Only applications without topology or deployments can be deleted");
        }
        applicationRepository.delete(application);
    }

    public List<ProjectApplication> listByProject(String projectId) {
        return applicationRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
    }

    public List<ProjectApplicationListItem> listItemsByProject(String projectId) {
        return applicationRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
            .stream()
            .map(application -> new ProjectApplicationListItem(application, canDelete(application.getId())))
            .toList();
    }

    public List<ProjectApplicationListItem> listItems() {
        return applicationRepository.findAllByOrderByCreatedAtDesc()
            .stream()
            .map(application -> new ProjectApplicationListItem(application, canDelete(application.getId())))
            .toList();
    }

    public ProjectApplication requireById(String applicationId) {
        return applicationRepository.findWithProjectById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found"));
    }

    public ProjectApplication requireByProjectId(String projectId, String applicationId) {
        ProjectApplication application = requireById(applicationId);
        if (!application.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Application not found in the selected project");
        }
        return application;
    }

    public void assertProjectMutable(ProjectApplication application) {
        projectService.assertMutable(application.getProject());
    }

    public boolean canDelete(String applicationId) {
        return !serviceDefinitionRepository.existsByApplicationId(applicationId)
            && !bindingRepository.existsByApplicationId(applicationId)
            && !deploymentDefinitionRepository.existsByApplicationId(applicationId)
            && !deploymentRunRepository.existsByApplicationId(applicationId);
    }

    private void validateCombination(ApplicationBuildType buildType, ApplicationServiceType serviceType) {
        if (buildType == ApplicationBuildType.LOCAL_CODE && serviceType == ApplicationServiceType.ECS) {
            throw new IllegalArgumentException("LOCAL_CODE is not supported with ECS in the first version");
        }
    }

    private void validateSourceLocation(ApplicationBuildType buildType, String sourceLocation) {
        if (sourceLocation == null || sourceLocation.isBlank()) {
            throw new IllegalArgumentException("Source location is required");
        }
        String value = sourceLocation.trim();
        if (buildType == ApplicationBuildType.LOCAL_CODE) {
            validateLocalDirectory(value);
            return;
        }
        validateDockerRepository(value);
    }

    private void validateLocalDirectory(String sourceLocation) {
        try {
            if (!Files.isDirectory(Path.of(sourceLocation))) {
                throw new IllegalArgumentException("Source location must be an existing local directory for LOCAL_CODE applications");
            }
        } catch (InvalidPathException exception) {
            throw new IllegalArgumentException("Source location must be a valid local directory path");
        }
    }

    private void validateDockerRepository(String sourceLocation) {
        boolean hasWhitespace = sourceLocation.chars().anyMatch(Character::isWhitespace);
        if (hasWhitespace || !sourceLocation.matches("[a-z0-9][a-z0-9._/:@-]*")) {
            throw new IllegalArgumentException("Source location must be a Docker repository reference for DOCKER applications");
        }
    }

    private String normalizeKey(String key) {
        return key.trim().toLowerCase(Locale.ROOT);
    }

    public record CreateApplicationCommand(
        String projectId,
        String key,
        String name,
        String description,
        ApplicationBuildType buildType,
        ApplicationServiceType serviceType,
        String runtime,
        String sourceLocation,
        Integer defaultPort
    ) {
    }

    public record UpdateApplicationCommand(
        String key,
        String name,
        String description,
        ApplicationBuildType buildType,
        ApplicationServiceType serviceType,
        String runtime,
        String sourceLocation,
        Integer defaultPort
    ) {
    }

    public record ProjectApplicationListItem(
        ProjectApplication application,
        boolean deletable
    ) {
        public boolean active() {
            return application.getStatus() == RecordStatus.ACTIVE;
        }

        public boolean projectMutable() {
            return application.getProject().getStatus() == RecordStatus.ACTIVE;
        }
    }
}
