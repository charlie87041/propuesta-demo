package com.cookiesstore.common.authorization.repository;

import com.cookiesstore.common.authorization.domain.UserDomainPermissionOverride;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDomainPermissionOverrideRepository extends JpaRepository<UserDomainPermissionOverride, Long> {

    Optional<UserDomainPermissionOverride> findByUserIdAndDomainCodeAndPermissionCode(
        Long userId,
        String domainCode,
        String permissionCode
    );

    List<UserDomainPermissionOverride> findByUserIdAndDomainCode(Long userId, String domainCode);
}
