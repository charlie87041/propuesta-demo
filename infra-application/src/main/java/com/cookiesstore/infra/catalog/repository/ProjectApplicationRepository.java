package com.cookiesstore.infra.catalog.repository;

import com.cookiesstore.infra.catalog.domain.ProjectApplication;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectApplicationRepository extends JpaRepository<ProjectApplication, String> {

    @Query("SELECT a FROM ProjectApplication a JOIN FETCH a.project WHERE a.id = :id")
    Optional<ProjectApplication> findWithProjectById(@Param("id") String id);

    List<ProjectApplication> findByProjectId(String projectId);

    List<ProjectApplication> findByProjectIdOrderByCreatedAtDesc(String projectId);

    List<ProjectApplication> findAllByOrderByCreatedAtDesc();

    Optional<ProjectApplication> findByProjectIdAndKeyIgnoreCase(String projectId, String key);

    Optional<ProjectApplication> findByProjectIdAndNameIgnoreCase(String projectId, String name);

    boolean existsByProjectId(String projectId);

    void deleteByProjectId(String projectId);
}
