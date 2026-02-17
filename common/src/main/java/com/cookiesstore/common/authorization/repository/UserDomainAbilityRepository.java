package com.cookiesstore.common.authorization.repository;

import com.cookiesstore.common.authorization.domain.UserDomainAbility;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDomainAbilityRepository extends JpaRepository<UserDomainAbility, Long> {

    Optional<UserDomainAbility> findByUserIdAndDomainCodeAndAbilityCode(Long userId, String domainCode, String abilityCode);

    List<UserDomainAbility> findByUserIdAndDomainCodeAndGrantedTrue(Long userId, String domainCode);
}
