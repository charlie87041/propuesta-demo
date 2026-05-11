package com.cookiesstore.infra.auth.repository;

import com.cookiesstore.infra.auth.domain.InfraUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InfraUserRepository extends JpaRepository<InfraUser, String> {

    Optional<InfraUser> findByUsernameIgnoreCase(String username);
}
