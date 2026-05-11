package com.cookiesstore.infra.topology.service;

import com.cookiesstore.infra.catalog.domain.ProjectApplication;
import com.cookiesstore.infra.catalog.domain.ProjectEnvironment;
import com.cookiesstore.infra.catalog.service.ProjectApplicationService;
import com.cookiesstore.infra.catalog.service.ProjectEnvironmentService;
import com.cookiesstore.infra.topology.domain.ApplicationResourceBinding;
import com.cookiesstore.infra.topology.domain.ProjectResourceDefinition;
import com.cookiesstore.infra.topology.repository.ApplicationResourceBindingRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ApplicationResourceBindingService {

    private final ProjectApplicationService applicationService;
    private final ProjectEnvironmentService environmentService;
    private final ProjectResourceDefinitionService resourceDefinitionService;
    private final ApplicationResourceBindingRepository repository;

    public ApplicationResourceBindingService(
        ProjectApplicationService applicationService,
        ProjectEnvironmentService environmentService,
        ProjectResourceDefinitionService resourceDefinitionService,
        ApplicationResourceBindingRepository repository
    ) {
        this.applicationService = applicationService;
        this.environmentService = environmentService;
        this.resourceDefinitionService = resourceDefinitionService;
        this.repository = repository;
    }

    @Transactional
    public ApplicationResourceBinding create(CreateApplicationResourceBindingCommand command) {
        ProjectApplication application = applicationService.requireById(command.applicationId());
        ProjectEnvironment environment = environmentService.requireById(command.environmentId());
        ProjectResourceDefinition resourceDefinition = resourceDefinitionService.requireById(command.projectResourceDefinitionId());

        validateSameProject(application, environment);
        applicationService.assertProjectMutable(application);
        environmentService.assertActive(environment);
        if (!resourceDefinition.getEnvironment().getId().equals(environment.getId())) {
            throw new IllegalArgumentException("The resource definition does not belong to the selected environment");
        }

        ApplicationResourceBinding binding = new ApplicationResourceBinding(
            application,
            environment,
            resourceDefinition,
            command.bindingType(),
            command.mountAs(),
            sanitizeConfig(command.configJson())
        );
        return repository.save(binding);
    }

    private void validateSameProject(ProjectApplication application, ProjectEnvironment environment) {
        if (!application.getProject().getId().equals(environment.getProject().getId())) {
            throw new IllegalArgumentException("The environment does not belong to the application's project");
        }
    }

    private String sanitizeConfig(String configJson) {
        return configJson == null || configJson.isBlank() ? "{}" : configJson;
    }

    public record CreateApplicationResourceBindingCommand(
        String applicationId,
        String environmentId,
        String projectResourceDefinitionId,
        com.cookiesstore.infra.topology.domain.BindingType bindingType,
        String mountAs,
        String configJson
    ) {
    }
}
