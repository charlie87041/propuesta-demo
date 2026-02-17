package com.cookiesstore.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cookiesstore.admin.domain.AdminUser;
import com.cookiesstore.admin.repository.AdminUserRepository;
import com.cookiesstore.common.authorization.domain.Ability;
import com.cookiesstore.common.authorization.domain.Domain;
import com.cookiesstore.common.authorization.domain.Permission;
import com.cookiesstore.common.authorization.domain.UserDomainAbility;
import com.cookiesstore.common.authorization.repository.AbilityRepository;
import com.cookiesstore.common.authorization.repository.DomainRepository;
import com.cookiesstore.common.authorization.repository.PermissionRepository;
import com.cookiesstore.common.authorization.repository.UserDomainAbilityRepository;
import com.cookiesstore.common.authorization.service.DomainAuthorizationService;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = AdminUserServiceIntegrationTest.TestConfig.class)
@org.springframework.transaction.annotation.Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:admin85;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AdminUserServiceIntegrationTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = {
        AdminUser.class,
        Domain.class,
        Ability.class,
        Permission.class,
        UserDomainAbility.class
    })
    @EnableJpaRepositories(basePackageClasses = {
        AdminUserRepository.class,
        DomainRepository.class,
        AbilityRepository.class,
        PermissionRepository.class,
        UserDomainAbilityRepository.class,
        com.cookiesstore.common.authorization.repository.UserDomainPermissionOverrideRepository.class
    })
    @Import({DomainAuthorizationService.class, AdminUserService.class, AdminAbilityAssignmentService.class})
    static class TestConfig {
    }

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private AbilityRepository abilityRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserDomainAbilityRepository userDomainAbilityRepository;

    @Test
    void createsAdminUser() {
        AdminUser saved = adminUserService.createAdminUser("service-create@cookies.dev", "Secret123!");

        assertTrue(saved.getId() != null);
        assertTrue(BCrypt.checkpw("Secret123!", saved.getPasswordHash()));
    }

    @Test
    void assignsAbilityToAdminUserInDomain() {
        AdminUser admin = createAdmin("assign@cookies.dev");
        Domain domain = createDomain("assign-domain");
        createAbility("manage-customers");

        adminUserService.assignAbility(999L, admin.getId(), domain.getCode(), "manage-customers");

        assertTrue(
            userDomainAbilityRepository
                .findByUserIdAndDomainCodeAndAbilityCode(admin.getId(), domain.getCode(), "manage-customers")
                .orElseThrow()
                .isGranted()
        );
    }

    @Test
    void revokesAbilityFromAdminUser() {
        AdminUser admin = createAdmin("revoke@cookies.dev");
        Domain domain = createDomain("revoke-domain");
        Ability ability = createAbility("manage-customers");

        UserDomainAbility grant = new UserDomainAbility();
        grant.setUserId(admin.getId());
        grant.setDomain(domain);
        grant.setAbility(ability);
        grant.setGranted(true);
        userDomainAbilityRepository.saveAndFlush(grant);

        adminUserService.revokeAbility(999L, admin.getId(), domain.getCode(), "manage-customers");

        assertFalse(
            userDomainAbilityRepository
                .findByUserIdAndDomainCodeAndAbilityCode(admin.getId(), domain.getCode(), "manage-customers")
                .orElseThrow()
                .isGranted()
        );
    }

    @Test
    void assignsPermissionOverrideAllowAndDeny() {
        AdminUser admin = createAdmin("override@cookies.dev");
        Domain domain = createDomain("override-domain");
        Permission permission = createPermission("customers:disable", "customers", "disable");

        adminUserService.assignPermissionOverride(999L, admin.getId(), domain.getCode(), permission.getCode(), true);
        assertTrue(adminUserService.listEffectivePermissions(admin.getId(), domain.getCode()).contains("customers:disable"));

        adminUserService.assignPermissionOverride(999L, admin.getId(), domain.getCode(), permission.getCode(), false);
        assertFalse(adminUserService.listEffectivePermissions(admin.getId(), domain.getCode()).contains("customers:disable"));
    }

    @Test
    void listsAdminUsersByDomain() {
        Domain domain = createDomain("list-domain");
        Ability ability = createAbility("manage-customers");

        AdminUser inDomain = createAdmin("in-domain@cookies.dev");
        AdminUser outsideDomain = createAdmin("outside-domain@cookies.dev");

        grantAbility(inDomain.getId(), domain, ability, true);

        List<AdminUser> users = adminUserService.listAdminUsersByDomain(domain.getCode());

        assertEquals(1, users.size());
        assertEquals("in-domain@cookies.dev", users.get(0).getEmail());
        assertFalse(users.stream().anyMatch(user -> user.getId().equals(outsideDomain.getId())));
    }

    @Test
    void listsAbilitiesOfAdminUser() {
        AdminUser admin = createAdmin("abilities@cookies.dev");
        Domain domain = createDomain("abilities-domain");
        Ability ability = createAbility("manage-customers");

        grantAbility(admin.getId(), domain, ability, true);

        Set<String> abilities = adminUserService.listAbilityCodes(admin.getId(), domain.getCode());

        assertTrue(abilities.contains("manage-customers"));
    }

    @Test
    void listsEffectivePermissionsOfAdminUser() {
        AdminUser admin = createAdmin("permissions@cookies.dev");
        Domain domain = createDomain("permissions-domain");
        Permission permission = createPermission("customers:list", "customers", "list");
        Ability ability = createAbility("manage-customers", permission);

        grantAbility(admin.getId(), domain, ability, true);

        Set<String> permissions = adminUserService.listEffectivePermissions(admin.getId(), domain.getCode());

        assertTrue(permissions.contains("customers:list"));
    }

    @Test
    void deactivatesAdminUserAndRevokesAllAbilities() {
        AdminUser admin = createAdmin("deactivate@cookies.dev");
        Domain domain = createDomain("deactivate-domain");
        Ability ability = createAbility("manage-customers");

        grantAbility(admin.getId(), domain, ability, true);

        adminUserService.deactivateAdminUser(admin.getId());

        AdminUser reloaded = adminUserRepository.findById(admin.getId()).orElseThrow();
        assertFalse(reloaded.isActive());
        assertFalse(
            userDomainAbilityRepository
                .findByUserIdAndDomainCodeAndAbilityCode(admin.getId(), domain.getCode(), "manage-customers")
                .orElseThrow()
                .isGranted()
        );
    }

    @Test
    void preventsSelfRevokingLastSuperAdminAbility() {
        AdminUser admin = createAdmin("super@cookies.dev");
        Domain domain = createDomain("super-domain");
        Ability superAdmin = createAbility("super-admin");

        grantAbility(admin.getId(), domain, superAdmin, true);

        assertThrows(
            IllegalStateException.class,
            () -> adminUserService.revokeAbility(admin.getId(), admin.getId(), domain.getCode(), "super-admin")
        );
    }

    private AdminUser createAdmin(String email) {
        return adminUserService.createAdminUser(email, "Secret123!");
    }

    private Domain createDomain(String code) {
        Domain domain = new Domain();
        domain.setCode(code);
        domain.setName(code);
        return domainRepository.saveAndFlush(domain);
    }

    private Ability createAbility(String code, Permission... permissions) {
        Ability ability = new Ability();
        ability.setCode(code);
        ability.setName(code);
        for (Permission permission : permissions) {
            ability.getPermissions().add(permission);
        }
        return abilityRepository.saveAndFlush(ability);
    }

    private Permission createPermission(String code, String resource, String action) {
        Permission permission = new Permission();
        permission.setCode(code);
        permission.setName(code);
        permission.setResource(resource);
        permission.setAction(action);
        return permissionRepository.saveAndFlush(permission);
    }

    private void grantAbility(Long userId, Domain domain, Ability ability, boolean granted) {
        UserDomainAbility grant = new UserDomainAbility();
        grant.setUserId(userId);
        grant.setDomain(domain);
        grant.setAbility(ability);
        grant.setGranted(granted);
        userDomainAbilityRepository.saveAndFlush(grant);
    }
}
