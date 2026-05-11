package com.cookiesstore.infra.catalog.service;

import com.cookiesstore.infra.catalog.domain.Project;
import com.cookiesstore.infra.catalog.repository.ProjectApplicationRepository;
import com.cookiesstore.infra.catalog.repository.ProjectCredentialRepository;
import com.cookiesstore.infra.catalog.repository.ProjectEnvironmentRepository;
import com.cookiesstore.infra.catalog.repository.ProjectRepository;
import com.cookiesstore.infra.deployment.repository.DeploymentDefinitionRepository;
import com.cookiesstore.infra.deployment.repository.DeploymentRunRepository;
import com.cookiesstore.infra.shared.domain.RecordStatus;
import com.cookiesstore.infra.topology.repository.ApplicationResourceBindingRepository;
import com.cookiesstore.infra.topology.repository.ApplicationServiceDefinitionRepository;
import com.cookiesstore.infra.topology.repository.ProjectResourceDefinitionRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final CloudProviderService cloudProviderService;
    private final ProjectCredentialService projectCredentialService;
    private final ProjectEnvironmentRepository environmentRepository;
    private final ProjectApplicationRepository applicationRepository;
    private final ProjectCredentialRepository credentialRepository;
    private final ProjectResourceDefinitionRepository resourceDefinitionRepository;
    private final ApplicationServiceDefinitionRepository serviceDefinitionRepository;
    private final ApplicationResourceBindingRepository bindingRepository;
    private final DeploymentDefinitionRepository deploymentDefinitionRepository;
    private final DeploymentRunRepository deploymentRunRepository;

    public ProjectService(
        ProjectRepository projectRepository,
        CloudProviderService cloudProviderService,
        ProjectCredentialService projectCredentialService,
        ProjectEnvironmentRepository environmentRepository,
        ProjectApplicationRepository applicationRepository,
        ProjectCredentialRepository credentialRepository,
        ProjectResourceDefinitionRepository resourceDefinitionRepository,
        ApplicationServiceDefinitionRepository serviceDefinitionRepository,
        ApplicationResourceBindingRepository bindingRepository,
        DeploymentDefinitionRepository deploymentDefinitionRepository,
        DeploymentRunRepository deploymentRunRepository
    ) {
        this.projectRepository = projectRepository;
        this.cloudProviderService = cloudProviderService;
        this.projectCredentialService = projectCredentialService;
        this.environmentRepository = environmentRepository;
        this.applicationRepository = applicationRepository;
        this.credentialRepository = credentialRepository;
        this.resourceDefinitionRepository = resourceDefinitionRepository;
        this.serviceDefinitionRepository = serviceDefinitionRepository;
        this.bindingRepository = bindingRepository;
        this.deploymentDefinitionRepository = deploymentDefinitionRepository;
        this.deploymentRunRepository = deploymentRunRepository;
    }

    @Transactional
    public Project create(CreateProjectCommand command) {
        String normalizedKey = normalizeKey(command.key());
        projectRepository.findByKeyIgnoreCase(normalizedKey)
            .ifPresent(project -> {
                throw new IllegalArgumentException("A project with that key already exists");
            });

        var provider = cloudProviderService.requireActiveByCode(command.providerCode());

        Project project = new Project(
            normalizedKey,
            command.name().trim(),
            command.description(),
            command.defaultRegion().trim(),
            command.owner(),
            provider.getCode()
        );
        Project savedProject = projectRepository.save(project);
        projectCredentialService.create(savedProject, provider, command.providerCredentialsJson());
        return savedProject;
    }

    @Transactional
    public Project update(String projectId, UpdateProjectCommand command) {
        Project project = requireMutableById(projectId);
        project.updateDetails(
            command.name().trim(),
            command.description(),
            command.defaultRegion().trim(),
            command.owner()
        );
        return projectRepository.save(project);
    }

    @Transactional
    public void archive(String projectId) {
        Project project = requireById(projectId);
        project.archive();
        projectRepository.save(project);
    }

    @Transactional
    public void activate(String projectId) {
        Project project = requireById(projectId);
        project.activate();
        projectRepository.save(project);
    }

    @Transactional
    public void deleteIfEmpty(String projectId) {
        Project project = requireMutableById(projectId);
        if (!canDelete(projectId)) {
            throw new IllegalArgumentException("Only projects without topology or deployments can be deleted");
        }
        credentialRepository.deleteByProjectId(project.getId());
        applicationRepository.deleteByProjectId(project.getId());
        environmentRepository.deleteByProjectId(project.getId());
        projectRepository.delete(project);
    }

    @Transactional
    public void updateProviderCredentials(String projectId, String providerCredentialsJson) {
        Project project = requireMutableById(projectId);
        var provider = cloudProviderService.requireActiveByCode(project.getProviderCode());
        projectCredentialService.create(project, provider, providerCredentialsJson);
    }

    public List<Project> list() {
        return projectRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Project> listActive() {
        return projectRepository.findAllByOrderByCreatedAtDesc()
            .stream()
            .filter(this::isMutable)
            .toList();
    }

    public boolean canDelete(String projectId) {
        return !resourceDefinitionRepository.existsByProjectId(projectId)
            && !serviceDefinitionRepository.existsByApplication_Project_Id(projectId)
            && !bindingRepository.existsByApplication_Project_Id(projectId)
            && !deploymentDefinitionRepository.existsByApplication_Project_Id(projectId)
            && !deploymentRunRepository.existsByProjectId(projectId);
    }

    public List<ProjectListItem> listItems() {
        return projectRepository.findAllByOrderByCreatedAtDesc()
            .stream()
            .map(project -> new ProjectListItem(project, canDelete(project.getId())))
            .toList();
    }

    public Project requireById(String projectId) {
        return projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));
    }

    public Project requireMutableById(String projectId) {
        Project project = requireById(projectId);
        assertMutable(project);
        return project;
    }

    public void assertMutable(Project project) {
        if (!isMutable(project)) {
            throw new IllegalArgumentException("Archived projects are read-only");
        }
    }

    public boolean isMutable(Project project) {
        return project.getStatus() == RecordStatus.ACTIVE;
    }

    private String normalizeKey(String key) {
        return key.trim().toLowerCase(Locale.ROOT);
    }

    public record CreateProjectCommand(
        String key,
        String name,
        String description,
        String defaultRegion,
        String owner,
        String providerCode,
        String providerCredentialsJson
    ) {
    }

    public record UpdateProjectCommand(
        String name,
        String description,
        String defaultRegion,
        String owner
    ) {
    }

    public record ProjectListItem(
        Project project,
        boolean deletable
    ) {
        public boolean active() {
            return project.getStatus() == RecordStatus.ACTIVE;
        }
    }
}
