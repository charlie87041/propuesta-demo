package com.cookiesstore.infra.topology.service;

import com.cookiesstore.infra.catalog.domain.ProjectApplication;
import com.cookiesstore.infra.catalog.domain.ProjectEnvironment;
import com.cookiesstore.infra.catalog.service.ProjectApplicationService;
import com.cookiesstore.infra.catalog.service.ProjectEnvironmentService;
import com.cookiesstore.infra.topology.domain.ApplicationServiceDefinition;
import com.cookiesstore.infra.topology.repository.ApplicationServiceDefinitionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ApplicationServiceDefinitionService {

    private final ProjectApplicationService applicationService;
    private final ProjectEnvironmentService environmentService;
    private final ApplicationServiceDefinitionRepository repository;

    public ApplicationServiceDefinitionService(
        ProjectApplicationService applicationService,
        ProjectEnvironmentService environmentService,
        ApplicationServiceDefinitionRepository repository
    ) {
        this.applicationService = applicationService;
        this.environmentService = environmentService;
        this.repository = repository;
    }

    @Transactional
    public ApplicationServiceDefinition create(CreateApplicationServiceDefinitionCommand command) {
        ProjectApplication application = applicationService.requireById(command.applicationId());
        ProjectEnvironment environment = environmentService.requireById(command.environmentId());
        validateSameProject(application, environment);
        applicationService.assertProjectMutable(application);
        environmentService.assertActive(environment);

        repository.findByApplicationIdAndEnvironmentId(application.getId(), environment.getId())
            .ifPresent(existing -> {
                throw new IllegalArgumentException("An application service definition already exists for that environment");
            });

        ApplicationServiceDefinition definition = new ApplicationServiceDefinition(
            application,
            environment,
            command.serviceType(),
            command.name().trim(),
            sanitizeConfig(command.configJson())
        );
        return repository.save(definition);
    }

    public ApplicationServiceDefinition requireById(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Application service definition not found"));
    }

    private void validateSameProject(ProjectApplication application, ProjectEnvironment environment) {
        if (!application.getProject().getId().equals(environment.getProject().getId())) {
            throw new IllegalArgumentException("The environment does not belong to the application's project");
        }
    }

    private String sanitizeConfig(String configJson) {
        return configJson == null || configJson.isBlank() ? "{}" : configJson;
    }

    public record CreateApplicationServiceDefinitionCommand(
        String applicationId,
        String environmentId,
        com.cookiesstore.infra.catalog.domain.ApplicationServiceType serviceType,
        String name,
        String configJson
    ) {
    }
}
