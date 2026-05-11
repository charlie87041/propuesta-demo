package com.cookiesstore.infra.catalog.repository;

import com.cookiesstore.infra.catalog.domain.ProjectEnvironment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectEnvironmentRepository extends JpaRepository<ProjectEnvironment, String> {

    List<ProjectEnvironment> findByProjectId(String projectId);

    List<ProjectEnvironment> findByProjectIdOrderByNameAsc(String projectId);

    Optional<ProjectEnvironment> findByProjectIdAndNameIgnoreCase(String projectId, String name);

    boolean existsByProjectId(String projectId);

    void deleteByProjectId(String projectId);
}
