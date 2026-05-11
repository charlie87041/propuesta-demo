package com.cookiesstore.infra.deployment.repository;

import com.cookiesstore.infra.deployment.domain.ResourceOutput;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceOutputRepository extends JpaRepository<ResourceOutput, String> {

    List<ResourceOutput> findByDeploymentRunId(String deploymentRunId);
}
