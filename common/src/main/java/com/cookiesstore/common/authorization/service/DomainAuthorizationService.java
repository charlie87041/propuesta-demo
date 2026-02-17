package com.cookiesstore.common.authorization.service;

import com.cookiesstore.common.authorization.domain.Permission;
import com.cookiesstore.common.authorization.domain.UserDomainAbility;
import com.cookiesstore.common.authorization.domain.UserDomainPermissionOverride;
import com.cookiesstore.common.authorization.repository.UserDomainAbilityRepository;
import com.cookiesstore.common.authorization.repository.UserDomainPermissionOverrideRepository;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class DomainAuthorizationService {

    private final UserDomainAbilityRepository userDomainAbilityRepository;
    private final UserDomainPermissionOverrideRepository overrideRepository;

    public DomainAuthorizationService(
        UserDomainAbilityRepository userDomainAbilityRepository,
        UserDomainPermissionOverrideRepository overrideRepository
    ) {
        this.userDomainAbilityRepository = userDomainAbilityRepository;
        this.overrideRepository = overrideRepository;
    }

    public boolean hasPermission(Long userId, String domainCode, String permissionCode) {
        Set<String> permissions = getPermissions(userId, domainCode);
        return permissions.contains("*") || permissions.contains(permissionCode);
    }

    public boolean hasAbility(Long userId, String domainCode, String abilityCode) {
        return userDomainAbilityRepository.findByUserIdAndDomainCodeAndAbilityCode(userId, domainCode, abilityCode)
            .map(UserDomainAbility::isGranted)
            .orElse(false);
    }

    public boolean hasDomainAccess(Long userId, String domainCode) {
        boolean hasGrantedAbility = !userDomainAbilityRepository
            .findByUserIdAndDomainCodeAndGrantedTrue(userId, domainCode)
            .isEmpty();
        if (hasGrantedAbility) {
            return true;
        }

        return !overrideRepository.findByUserIdAndDomainCode(userId, domainCode).isEmpty();
    }

    public Set<String> getPermissions(Long userId, String domainCode) {
        Set<String> resolved = new LinkedHashSet<>();

        for (UserDomainAbility grant : userDomainAbilityRepository.findByUserIdAndDomainCodeAndGrantedTrue(userId, domainCode)) {
            for (Permission permission : grant.getAbility().getPermissions()) {
                resolved.add(permission.getCode());
            }
        }

        for (UserDomainPermissionOverride override : overrideRepository.findByUserIdAndDomainCode(userId, domainCode)) {
            String code = override.getPermission().getCode();
            if (override.isGranted()) {
                resolved.add(code);
            } else {
                resolved.remove(code);
            }
        }

        return resolved;
    }
}
