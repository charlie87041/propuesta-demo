package com.cookiesstore.infra.topology.service;

import com.cookiesstore.infra.catalog.domain.Project;
import com.cookiesstore.infra.catalog.domain.ProjectEnvironment;
import com.cookiesstore.infra.catalog.service.ProjectEnvironmentService;
import com.cookiesstore.infra.catalog.service.ProjectService;
import com.cookiesstore.infra.shared.domain.RecordStatus;
import com.cookiesstore.infra.topology.domain.ProjectResourceDefinition;
import com.cookiesstore.infra.topology.domain.ResourceType;
import com.cookiesstore.infra.topology.repository.ApplicationResourceBindingRepository;
import com.cookiesstore.infra.topology.repository.ProjectResourceDefinitionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProjectResourceDefinitionService {

    private final ProjectService projectService;
    private final ProjectEnvironmentService environmentService;
    private final ProjectResourceDefinitionRepository repository;
    private final ApplicationResourceBindingRepository bindingRepository;
    private final ObjectMapper objectMapper;

    public ProjectResourceDefinitionService(
        ProjectService projectService,
        ProjectEnvironmentService environmentService,
        ProjectResourceDefinitionRepository repository,
        ApplicationResourceBindingRepository bindingRepository,
        ObjectMapper objectMapper
    ) {
        this.projectService = projectService;
        this.environmentService = environmentService;
        this.repository = repository;
        this.bindingRepository = bindingRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ProjectResourceDefinition create(CreateProjectResourceDefinitionCommand command) {
        Project project = projectService.requireMutableById(command.projectId());
        ProjectEnvironment environment = environmentService.requireById(command.environmentId());
        validateSameProject(project, environment);
        environmentService.assertActive(environment);

        ProjectResourceDefinition definition = new ProjectResourceDefinition(
            project,
            environment,
            command.resourceType(),
            command.name().trim(),
            command.description(),
            sanitizeConfig(command.configJson())
        );
        return repository.save(definition);
    }

    @Transactional
    public ProjectResourceDefinition update(String id, UpdateProjectResourceDefinitionCommand command) {
        ProjectResourceDefinition definition = requireById(id);
        definition.updateDetails(
            command.name().trim(),
            command.description(),
            sanitizeConfig(command.configJson())
        );
        return definition;
    }

    @Transactional
    public void archive(String id) {
        requireById(id).archive();
    }

    @Transactional
    public void activate(String id) {
        requireById(id).activate();
    }

    @Transactional
    public void deleteIfUnused(String id) {
        ProjectResourceDefinition definition = requireById(id);
        if (bindingRepository.existsByProjectResourceDefinitionId(id)) {
            throw new IllegalArgumentException("This resource has active application bindings and cannot be deleted");
        }
        repository.delete(definition);
    }

    public List<ResourceDefinitionListItem> listItemsByEnvironment(String environmentId) {
        return repository.findByEnvironmentId(environmentId)
            .stream()
            .map(d -> new ResourceDefinitionListItem(d, configSummary(d), canDelete(d.getId())))
            .toList();
    }

    public List<ProjectResourceDefinition> listByEnvironment(String environmentId) {
        return repository.findByEnvironmentId(environmentId);
    }

    public ProjectResourceDefinition requireById(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Project resource definition not found"));
    }

    public boolean canDelete(String id) {
        return !bindingRepository.existsByProjectResourceDefinitionId(id);
    }

    private void validateSameProject(Project project, ProjectEnvironment environment) {
        if (!environment.getProject().getId().equals(project.getId())) {
            throw new IllegalArgumentException("The environment does not belong to the selected project");
        }
    }

    private String sanitizeConfig(String configJson) {
        return configJson == null || configJson.isBlank() ? "{}" : configJson;
    }

    private String configSummary(ProjectResourceDefinition definition) {
        if (definition.getResourceType() == ResourceType.VPC) {
            try {
                JsonNode node = objectMapper.readTree(definition.getConfigJson());
                JsonNode cidr = node.get("cidrBlock");
                return cidr != null && !cidr.isNull() ? cidr.asText() : "";
            } catch (JsonProcessingException e) {
                return "";
            }
        }
        return "";
    }

    public record CreateProjectResourceDefinitionCommand(
        String projectId,
        String environmentId,
        ResourceType resourceType,
        String name,
        String description,
        String configJson
    ) {
    }

    public record UpdateProjectResourceDefinitionCommand(
        String name,
        String description,
        String configJson
    ) {
    }

    public record ResourceDefinitionListItem(
        ProjectResourceDefinition definition,
        String configSummary,
        boolean deletable
    ) {
        public boolean active() {
            return definition.getStatus() == RecordStatus.ACTIVE;
        }
    }
}
