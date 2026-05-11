package com.cookiesstore.infra.catalog.repository;

import com.cookiesstore.infra.catalog.domain.ProjectCredential;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectCredentialRepository extends JpaRepository<ProjectCredential, String> {

    Optional<ProjectCredential> findFirstByProjectIdOrderByCreatedAtDesc(String projectId);

    void deleteByProjectId(String projectId);
}
