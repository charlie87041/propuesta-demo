package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.Source;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long>, JpaSpecificationExecutor<Source> {

    Optional<Source> findByCode(String code);

    boolean existsByCode(String code);

    Optional<Source> findBySystemManagedTrue();

    List<Source> findAllBySystemManagedTrue();
}
