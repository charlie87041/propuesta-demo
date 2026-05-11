package com.cookiesstore.infra.topology.repository;

import com.cookiesstore.infra.topology.domain.ApplicationServiceDefinition;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationServiceDefinitionRepository extends JpaRepository<ApplicationServiceDefinition, String> {

    List<ApplicationServiceDefinition> findByApplicationId(String applicationId);

    Optional<ApplicationServiceDefinition> findByApplicationIdAndEnvironmentId(String applicationId, String environmentId);

    boolean existsByApplicationId(String applicationId);

    boolean existsByEnvironmentId(String environmentId);

    boolean existsByApplication_Project_Id(String projectId);
}
