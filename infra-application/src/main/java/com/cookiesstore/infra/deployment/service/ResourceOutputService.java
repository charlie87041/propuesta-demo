package com.cookiesstore.infra.deployment.service;

import com.cookiesstore.infra.deployment.domain.DeploymentRun;
import com.cookiesstore.infra.deployment.domain.ResourceOutput;
import com.cookiesstore.infra.deployment.repository.DeploymentRunRepository;
import com.cookiesstore.infra.deployment.repository.ResourceOutputRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ResourceOutputService {

    private final DeploymentRunRepository deploymentRunRepository;
    private final ResourceOutputRepository resourceOutputRepository;

    public ResourceOutputService(
        DeploymentRunRepository deploymentRunRepository,
        ResourceOutputRepository resourceOutputRepository
    ) {
        this.deploymentRunRepository = deploymentRunRepository;
        this.resourceOutputRepository = resourceOutputRepository;
    }

    @Transactional
    public ResourceOutput create(CreateResourceOutputCommand command) {
        DeploymentRun deploymentRun = deploymentRunRepository.findById(command.deploymentRunId())
            .orElseThrow(() -> new IllegalArgumentException("Deployment run not found"));

        ResourceOutput output = new ResourceOutput(
            deploymentRun,
            command.resourceType(),
            command.resourceName(),
            command.outputKey(),
            command.outputValue()
        );
        return resourceOutputRepository.save(output);
    }

    public List<ResourceOutput> listByDeploymentRun(String deploymentRunId) {
        return resourceOutputRepository.findByDeploymentRunId(deploymentRunId);
    }

    public record CreateResourceOutputCommand(
        String deploymentRunId,
        String resourceType,
        String resourceName,
        String outputKey,
        String outputValue
    ) {
    }
}
