package com.cookiesstore.common.authorization.repository;

import com.cookiesstore.common.authorization.domain.Ability;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AbilityRepository extends JpaRepository<Ability, Long> {

    Optional<Ability> findByCode(String code);
}
