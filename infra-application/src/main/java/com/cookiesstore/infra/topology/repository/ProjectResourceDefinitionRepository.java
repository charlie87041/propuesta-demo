package com.cookiesstore.infra.topology.repository;

import com.cookiesstore.infra.topology.domain.ProjectResourceDefinition;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectResourceDefinitionRepository extends JpaRepository<ProjectResourceDefinition, String> {

    List<ProjectResourceDefinition> findByEnvironmentId(String environmentId);

    boolean existsByEnvironmentId(String environmentId);

    boolean existsByProjectId(String projectId);
}
