package com.cookiesstore.common.authorization.repository;

import com.cookiesstore.common.authorization.domain.Domain;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomainRepository extends JpaRepository<Domain, Long> {

    Optional<Domain> findByCode(String code);
}
