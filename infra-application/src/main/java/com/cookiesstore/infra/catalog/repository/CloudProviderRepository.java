package com.cookiesstore.infra.catalog.repository;

import com.cookiesstore.infra.catalog.domain.CloudProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CloudProviderRepository extends JpaRepository<CloudProvider, String> {

    Optional<CloudProvider> findByCodeIgnoreCase(String code);
}
