package com.cookiesstore.common.authorization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cookiesstore.common.TestApplication;
import com.cookiesstore.common.authorization.domain.Ability;
import com.cookiesstore.common.authorization.domain.Domain;
import com.cookiesstore.common.authorization.domain.Permission;
import com.cookiesstore.common.authorization.domain.UserDomainAbility;
import com.cookiesstore.common.authorization.domain.UserDomainPermissionOverride;
import com.cookiesstore.common.authorization.repository.AbilityRepository;
import com.cookiesstore.common.authorization.repository.DomainRepository;
import com.cookiesstore.common.authorization.repository.PermissionRepository;
import com.cookiesstore.common.authorization.repository.UserDomainAbilityRepository;
import com.cookiesstore.common.authorization.repository.UserDomainPermissionOverrideRepository;
import com.cookiesstore.common.authorization.service.DomainAuthorizationService;
import com.cookiesstore.common.test.AbstractIntegrationTest;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
class DomainAuthorizationServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private DomainAuthorizationService authorizationService;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private AbilityRepository abilityRepository;

    @Autowired
    private UserDomainAbilityRepository userDomainAbilityRepository;

    @Autowired
    private UserDomainPermissionOverrideRepository overrideRepository;

    @Test
    void hasPermissionReturnsTrueWhenAbilityIncludesPermission() {
        Domain domain = createDomain("auth-main");
        Permission permission = createPermission("products:list");
        Ability ability = createAbilityWithPermissions("browse-catalog", permission);
        grantAbility(10L, domain, ability, true);

        assertTrue(authorizationService.hasPermission(10L, "auth-main", "products:list"));
    }

    @Test
    void hasPermissionReturnsFalseWhenNoAbilityGranted() {
        createDomain("auth-no-ability");

        assertFalse(authorizationService.hasPermission(11L, "auth-no-ability", "products:list"));
    }

    @Test
    void hasPermissionReturnsFalseForDifferentDomain() {
        Domain domainA = createDomain("domain-a");
        createDomain("domain-b");
        Permission permission = createPermission("orders:list");
        Ability ability = createAbilityWithPermissions("manage-orders", permission);
        grantAbility(12L, domainA, ability, true);

        assertFalse(authorizationService.hasPermission(12L, "domain-b", "orders:list"));
    }

    @Test
    void denyOverrideTakesPrecedenceOverAbilityGrant() {
        Domain domain = createDomain("override-deny");
        Permission permission = createPermission("customers:update");
        Ability ability = createAbilityWithPermissions("manage-customers", permission);
        grantAbility(13L, domain, ability, true);
        createOverride(13L, domain, permission, false);

        assertFalse(authorizationService.hasPermission(13L, "override-deny", "customers:update"));
    }

    @Test
    void allowOverrideAddsPermissionWithoutAbility() {
        Domain domain = createDomain("override-allow");
        Permission permission = createPermission("orders:refund");
        createOverride(14L, domain, permission, true);

        assertTrue(authorizationService.hasPermission(14L, "override-allow", "orders:refund"));
    }

    @Test
    void hasAbilityWorksCorrectly() {
        Domain domain = createDomain("ability-check");
        Ability ability = createAbilityWithPermissions("view-reports", createPermission("reports:sales"));
        grantAbility(15L, domain, ability, true);

        assertTrue(authorizationService.hasAbility(15L, "ability-check", "view-reports"));
        assertFalse(authorizationService.hasAbility(15L, "ability-check", "manage-settings"));
    }

    @Test
    void hasDomainAccessWorksCorrectly() {
        Domain domain = createDomain("domain-access");
        Ability ability = createAbilityWithPermissions("manage-profile", createPermission("profile:update"));

        assertFalse(authorizationService.hasDomainAccess(16L, "domain-access"));

        grantAbility(16L, domain, ability, true);
        assertTrue(authorizationService.hasDomainAccess(16L, "domain-access"));
    }

    @Test
    void getPermissionsReturnsFullSetWithOverridesApplied() {
        Domain domain = createDomain("perm-set");
        Permission list = createPermission("products:list");
        Permission update = createPermission("products:update");
        Permission delete = createPermission("products:delete");

        Ability manageInventory = createAbilityWithPermissions("manage-inventory", list, update);
        grantAbility(17L, domain, manageInventory, true);

        createOverride(17L, domain, update, false);
        createOverride(17L, domain, delete, true);

        Set<String> permissions = authorizationService.getPermissions(17L, "perm-set");

        assertTrue(permissions.contains("products:list"));
        assertFalse(permissions.contains("products:update"));
        assertTrue(permissions.contains("products:delete"));
    }

    private Domain createDomain(String code) {
        Domain domain = new Domain();
        domain.setCode(code);
        domain.setName(code);
        return domainRepository.save(domain);
    }

    private Permission createPermission(String code) {
        Permission permission = new Permission();
        permission.setCode(code);
        permission.setName(code);
        String[] parts = code.split(":");
        permission.setResource(parts[0]);
        permission.setAction(parts[1]);
        return permissionRepository.save(permission);
    }

    private Ability createAbilityWithPermissions(String code, Permission... permissions) {
        Ability ability = new Ability();
        ability.setCode(code);
        ability.setName(code);
        for (Permission permission : permissions) {
            ability.getPermissions().add(permission);
        }
        return abilityRepository.save(ability);
    }

    private void grantAbility(Long userId, Domain domain, Ability ability, boolean granted) {
        UserDomainAbility grant = new UserDomainAbility();
        grant.setUserId(userId);
        grant.setDomain(domain);
        grant.setAbility(ability);
        grant.setGranted(granted);
        userDomainAbilityRepository.save(grant);
    }

    private void createOverride(Long userId, Domain domain, Permission permission, boolean granted) {
        UserDomainPermissionOverride override = new UserDomainPermissionOverride();
        override.setUserId(userId);
        override.setDomain(domain);
        override.setPermission(permission);
        override.setGranted(granted);
        overrideRepository.save(override);
    }
}
