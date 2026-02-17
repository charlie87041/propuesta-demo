package com.cookiesstore.admin.service;

import com.cookiesstore.admin.domain.AdminUser;
import com.cookiesstore.admin.repository.AdminUserRepository;
import com.cookiesstore.common.authorization.domain.Domain;
import com.cookiesstore.common.authorization.domain.Permission;
import com.cookiesstore.common.authorization.domain.UserDomainPermissionOverride;
import com.cookiesstore.common.authorization.domain.UserDomainAbility;
import com.cookiesstore.common.authorization.repository.DomainRepository;
import com.cookiesstore.common.authorization.repository.PermissionRepository;
import com.cookiesstore.common.authorization.repository.UserDomainAbilityRepository;
import com.cookiesstore.common.authorization.repository.UserDomainPermissionOverrideRepository;
import com.cookiesstore.common.authorization.service.DomainAuthorizationService;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final AdminAbilityAssignmentService abilityAssignmentService;
    private final DomainRepository domainRepository;
    private final PermissionRepository permissionRepository;
    private final UserDomainPermissionOverrideRepository overrideRepository;
    private final UserDomainAbilityRepository userDomainAbilityRepository;
    private final DomainAuthorizationService domainAuthorizationService;

    public AdminUserService(
        AdminUserRepository adminUserRepository,
        AdminAbilityAssignmentService abilityAssignmentService,
        DomainRepository domainRepository,
        PermissionRepository permissionRepository,
        UserDomainPermissionOverrideRepository overrideRepository,
        UserDomainAbilityRepository userDomainAbilityRepository,
        DomainAuthorizationService domainAuthorizationService
    ) {
        this.adminUserRepository = adminUserRepository;
        this.abilityAssignmentService = abilityAssignmentService;
        this.domainRepository = domainRepository;
        this.permissionRepository = permissionRepository;
        this.overrideRepository = overrideRepository;
        this.userDomainAbilityRepository = userDomainAbilityRepository;
        this.domainAuthorizationService = domainAuthorizationService;
    }

    public AdminUser createAdminUser(String email, String rawPassword) {
        if (adminUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Admin user with email already exists: " + email);
        }

        AdminUser adminUser = new AdminUser();
        adminUser.setEmail(email);
        adminUser.setPassword(rawPassword);
        adminUser.setActive(true);
        return adminUserRepository.save(adminUser);
    }

    public void assignAbility(Long actorUserId, Long targetUserId, String domainCode, String abilityCode) {
        ensureAdminUserExists(targetUserId);
        abilityAssignmentService.assignAbility(actorUserId, targetUserId, domainCode, abilityCode);
    }

    public void revokeAbility(Long actorUserId, Long targetUserId, String domainCode, String abilityCode) {
        ensureAdminUserExists(targetUserId);
        abilityAssignmentService.revokeAbility(actorUserId, targetUserId, domainCode, abilityCode);
    }

    public void assignPermissionOverride(
        Long actorUserId,
        Long targetUserId,
        String domainCode,
        String permissionCode,
        boolean granted
    ) {
        ensureAdminUserExists(targetUserId);

        Domain domain = domainRepository.findByCode(domainCode)
            .orElseThrow(() -> new IllegalArgumentException("Domain not found: " + domainCode));
        Permission permission = permissionRepository.findByCode(permissionCode)
            .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionCode));

        UserDomainPermissionOverride override = overrideRepository
            .findByUserIdAndDomainCodeAndPermissionCode(targetUserId, domainCode, permissionCode)
            .orElseGet(UserDomainPermissionOverride::new);

        override.setUserId(targetUserId);
        override.setDomain(domain);
        override.setPermission(permission);
        override.setGranted(granted);
        override.setGrantedBy(actorUserId);

        overrideRepository.save(override);
    }

    @Transactional(readOnly = true)
    public List<AdminUser> listAdminUsersByDomain(String domainCode) {
        Set<Long> userIds = userDomainAbilityRepository.findByDomainCodeAndGrantedTrue(domainCode)
            .stream()
            .map(UserDomainAbility::getUserId)
            .collect(LinkedHashSet::new, Set::add, Set::addAll);

        if (userIds.isEmpty()) {
            return List.of();
        }

        List<AdminUser> users = new ArrayList<>(adminUserRepository.findAllById(userIds));
        users.removeIf(user -> !user.isActive());
        return users;
    }

    @Transactional(readOnly = true)
    public Set<String> listAbilityCodes(Long userId, String domainCode) {
        ensureAdminUserExists(userId);
        return abilityAssignmentService.listAbilityCodes(userId, domainCode);
    }

    @Transactional(readOnly = true)
    public Set<String> listEffectivePermissions(Long userId, String domainCode) {
        ensureAdminUserExists(userId);
        return domainAuthorizationService.getPermissions(userId, domainCode);
    }

    public void deactivateAdminUser(Long userId) {
        AdminUser adminUser = ensureAdminUserExists(userId);
        adminUser.setActive(false);
        adminUserRepository.save(adminUser);
        abilityAssignmentService.revokeAllAbilitiesForUser(userId);
    }

    private AdminUser ensureAdminUserExists(Long userId) {
        return adminUserRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Admin user not found: " + userId));
    }
}
