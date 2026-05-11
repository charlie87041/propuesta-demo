package com.cookiesstore.infra.deployment.service;

import com.cookiesstore.infra.catalog.domain.ProjectApplication;
import com.cookiesstore.infra.catalog.domain.ProjectEnvironment;
import com.cookiesstore.infra.catalog.service.ProjectApplicationService;
import com.cookiesstore.infra.catalog.service.ProjectEnvironmentService;
import com.cookiesstore.infra.deployment.domain.DeploymentDefinition;
import com.cookiesstore.infra.deployment.repository.DeploymentDefinitionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class DeploymentDefinitionService {

    private final ProjectApplicationService applicationService;
    private final ProjectEnvironmentService environmentService;
    private final DeploymentDefinitionRepository repository;

    public DeploymentDefinitionService(
        ProjectApplicationService applicationService,
        ProjectEnvironmentService environmentService,
        DeploymentDefinitionRepository repository
    ) {
        this.applicationService = applicationService;
        this.environmentService = environmentService;
        this.repository = repository;
    }

    @Transactional
    public DeploymentDefinition create(CreateDeploymentDefinitionCommand command) {
        ProjectApplication application = applicationService.requireById(command.applicationId());
        ProjectEnvironment environment = environmentService.requireById(command.environmentId());
        validateSameProject(application, environment, command.buildType());
        applicationService.assertProjectMutable(application);
        environmentService.assertActive(environment);

        repository.findByApplicationIdAndEnvironmentId(application.getId(), environment.getId())
            .ifPresent(existing -> {
                throw new IllegalArgumentException("A deployment definition already exists for that application and environment");
            });

        DeploymentDefinition definition = new DeploymentDefinition(
            application,
            environment,
            command.buildType(),
            command.artifactSource(),
            command.entrypoint(),
            sanitizeConfig(command.deployConfigJson())
        );
        return repository.save(definition);
    }

    private void validateSameProject(
        ProjectApplication application,
        ProjectEnvironment environment,
        com.cookiesstore.infra.catalog.domain.ApplicationBuildType buildType
    ) {
        if (!application.getProject().getId().equals(environment.getProject().getId())) {
            throw new IllegalArgumentException("The environment does not belong to the application's project");
        }
        if (application.getBuildType() != buildType) {
            throw new IllegalArgumentException("The deployment build type must match the application's build type");
        }
    }

    private String sanitizeConfig(String configJson) {
        return configJson == null || configJson.isBlank() ? "{}" : configJson;
    }

    public record CreateDeploymentDefinitionCommand(
        String applicationId,
        String environmentId,
        com.cookiesstore.infra.catalog.domain.ApplicationBuildType buildType,
        String artifactSource,
        String entrypoint,
        String deployConfigJson
    ) {
    }
}
