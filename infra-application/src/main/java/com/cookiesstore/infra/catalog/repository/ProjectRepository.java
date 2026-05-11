package com.cookiesstore.infra.catalog.repository;

import com.cookiesstore.infra.catalog.domain.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, String> {

    List<Project> findAllByOrderByCreatedAtDesc();

    Optional<Project> findByKeyIgnoreCase(String key);
}
