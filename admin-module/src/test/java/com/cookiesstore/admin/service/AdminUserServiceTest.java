package com.cookiesstore.admin.service;

import com.cookiesstore.admin.domain.AdminUser;
import com.cookiesstore.admin.repository.AdminUserRepository;
import com.cookiesstore.common.authorization.domain.Domain;
import com.cookiesstore.common.authorization.domain.Permission;
import com.cookiesstore.common.authorization.domain.UserDomainPermissionOverride;
import com.cookiesstore.common.authorization.repository.AbilityRepository;
import com.cookiesstore.common.authorization.repository.DomainRepository;
import com.cookiesstore.common.authorization.repository.PermissionRepository;
import com.cookiesstore.common.authorization.repository.UserDomainAbilityRepository;
import com.cookiesstore.common.authorization.repository.UserDomainPermissionOverrideRepository;
import com.cookiesstore.common.authorization.service.DomainAuthorizationService;
import com.cookiesstore.common.authorization.domain.UserDomainAbility;
import java.util.Optional;
import java.util.Set;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private AdminUserRepository adminUserRepository;
    @Mock
    private AdminAbilityAssignmentService abilityAssignmentService;
    @Mock
    private AbilityRepository abilityRepository;
    @Mock
    private DomainRepository domainRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private UserDomainPermissionOverrideRepository overrideRepository;
    @Mock
    private UserDomainAbilityRepository userDomainAbilityRepository;
    @Mock
    private DomainAuthorizationService domainAuthorizationService;

    @InjectMocks
    private AdminUserService adminUserService;

    @Test
    void syncPermissionOverrides_savesDeniedAndDeletesAllowedOverrides() {
        Long actorUserId = 10L;
        Long targetUserId = 22L;
        String domainCode = "main-store";
        String allowedCode = "users:list";
        String deniedCode = "users:delete";

        AdminUser adminUser = new AdminUser();
        adminUser.setEmail("admin@cookies.test");
        adminUser.setPassword("secret123");

        Domain domain = new Domain();
        domain.setCode(domainCode);
        domain.setName("Main Store");

        Permission deniedPermission = new Permission();
        deniedPermission.setCode(deniedCode);
        deniedPermission.setName("Delete users");
        deniedPermission.setResource("users");
        deniedPermission.setAction("delete");

        Permission allowedPermission = new Permission();
        allowedPermission.setCode(allowedCode);
        allowedPermission.setName("List users");
        allowedPermission.setResource("users");
        allowedPermission.setAction("list");

        UserDomainPermissionOverride existingAllowedOverride = new UserDomainPermissionOverride();
        existingAllowedOverride.setUserId(targetUserId);
        existingAllowedOverride.setDomain(domain);
        existingAllowedOverride.setPermission(allowedPermission);
        existingAllowedOverride.setGranted(true);

        when(adminUserRepository.findById(targetUserId)).thenReturn(Optional.of(adminUser));
        when(domainRepository.findByCode(domainCode)).thenReturn(Optional.of(domain));
        when(permissionRepository.findByCode(deniedCode)).thenReturn(Optional.of(deniedPermission));
        when(overrideRepository.findByUserIdAndDomainCodeAndPermissionCode(targetUserId, domainCode, deniedCode))
            .thenReturn(Optional.empty());
        when(overrideRepository.findByUserIdAndDomainCodeAndPermissionCode(targetUserId, domainCode, allowedCode))
            .thenReturn(Optional.of(existingAllowedOverride));

        adminUserService.syncPermissionOverrides(
            actorUserId,
            targetUserId,
            domainCode,
            Set.of(allowedCode, deniedCode),
            Set.of(deniedCode)
        );

        verify(overrideRepository).save(argThat(override ->
            override.getUserId().equals(targetUserId)
                && override.getPermission().getCode().equals(deniedCode)
                && !override.isGranted()
                && override.getGrantedBy().equals(actorUserId)
        ));
        verify(overrideRepository).delete(existingAllowedOverride);
    }

    @Test
    void listAdminUsersByDomain_includesUsersWithRevokedAbilities() {
        String domainCode = "main-store";
        Long activeUserId = 1L;
        Long inactiveUserId = 2L;

        UserDomainAbility activeGrant = new UserDomainAbility();
        activeGrant.setUserId(activeUserId);

        UserDomainAbility revokedGrant = new UserDomainAbility();
        revokedGrant.setUserId(inactiveUserId);

        AdminUser activeUser = new AdminUser();
        activeUser.setEmail("active@cookies.test");
        activeUser.setPassword("secret123");

        AdminUser inactiveUser = new AdminUser();
        inactiveUser.setEmail("inactive@cookies.test");
        inactiveUser.setPassword("secret123");

        when(userDomainAbilityRepository.findByDomainCode(domainCode))
            .thenReturn(List.of(activeGrant, revokedGrant));
        when(adminUserRepository.findAllById(anySet()))
            .thenReturn(List.of(activeUser, inactiveUser));

        List<AdminUser> result = adminUserService.listAdminUsersByDomain(domainCode);

        assertThat(result).hasSize(2);
    }
}
