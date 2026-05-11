package com.cookiesstore.infra.topology.repository;

import com.cookiesstore.infra.topology.domain.ApplicationResourceBinding;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationResourceBindingRepository extends JpaRepository<ApplicationResourceBinding, String> {

    List<ApplicationResourceBinding> findByApplicationIdAndEnvironmentId(String applicationId, String environmentId);

    boolean existsByApplicationId(String applicationId);

    boolean existsByEnvironmentId(String environmentId);

    boolean existsByApplication_Project_Id(String projectId);

    boolean existsByProjectResourceDefinitionId(String projectResourceDefinitionId);
}
