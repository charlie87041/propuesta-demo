package com.cookiesstore.infra.deployment.repository;

import com.cookiesstore.infra.deployment.domain.DeploymentDefinition;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeploymentDefinitionRepository extends JpaRepository<DeploymentDefinition, String> {

    List<DeploymentDefinition> findByApplicationId(String applicationId);

    Optional<DeploymentDefinition> findByApplicationIdAndEnvironmentId(String applicationId, String environmentId);

    boolean existsByApplicationId(String applicationId);

    boolean existsByEnvironmentId(String environmentId);

    boolean existsByApplication_Project_Id(String projectId);
}
