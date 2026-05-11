package com.cookiesstore.infra.deployment.repository;

import com.cookiesstore.infra.deployment.domain.DeploymentRun;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeploymentRunRepository extends JpaRepository<DeploymentRun, String> {

    List<DeploymentRun> findByProjectIdOrderByCreatedAtDesc(String projectId);

    boolean existsByApplicationId(String applicationId);

    boolean existsByEnvironmentId(String environmentId);

    boolean existsByProjectId(String projectId);
}
