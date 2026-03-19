package com.cookiesstore.admin.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cookiesstore.admin.domain.AdminUser;
import com.cookiesstore.admin.repository.AdminUserRepository;
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
import jakarta.persistence.Table;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@ContextConfiguration(classes = AdminUserRepositoryIntegrationTest.TestConfig.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:admin84;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AdminUserRepositoryIntegrationTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = {
        AdminUser.class,
        Domain.class,
        Ability.class,
        Permission.class,
        UserDomainAbility.class,
        UserDomainPermissionOverride.class
    })
    @EnableJpaRepositories(basePackageClasses = {
        AdminUserRepository.class,
        DomainRepository.class,
        AbilityRepository.class,
        PermissionRepository.class,
        UserDomainAbilityRepository.class,
        UserDomainPermissionOverrideRepository.class
    })
    static class TestConfig {
    }

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

    @Autowired
    private UserDomainPermissionOverrideRepository overrideRepository;

    @Test
    void createsAdminUserWithUniqueEmailConstraint() {
        AdminUser first = new AdminUser();
        first.setEmail("ops@cookies.dev");
        first.setPassword("Secret123!");
        adminUserRepository.saveAndFlush(first);

        AdminUser duplicate = new AdminUser();
        duplicate.setEmail("ops@cookies.dev");
        duplicate.setPassword("AnotherSecret123!");

        assertThrows(DataIntegrityViolationException.class, () -> adminUserRepository.saveAndFlush(duplicate));
    }

    @Test
    void findsAdminUserByEmail() {
        AdminUser adminUser = new AdminUser();
        adminUser.setEmail("security@cookies.dev");
        adminUser.setPassword("Secret123!");
        adminUserRepository.saveAndFlush(adminUser);

        AdminUser found = adminUserRepository.findByEmail("security@cookies.dev").orElseThrow();

        assertEquals("security@cookies.dev", found.getEmail());
    }

    @Test
    void hashesPasswordWithBcrypt() {
        String rawPassword = "PlainPassword123!";

        AdminUser adminUser = new AdminUser();
        adminUser.setEmail("hash@cookies.dev");
        adminUser.setPassword(rawPassword);

        AdminUser saved = adminUserRepository.saveAndFlush(adminUser);

        assertNotEquals(rawPassword, saved.getPasswordHash());
        assertTrue(BCrypt.checkpw(rawPassword, saved.getPasswordHash()));
    }

    @Test
    void usesDedicatedAdminUsersTableDifferentFromCustomer() {
        Table table = AdminUser.class.getAnnotation(Table.class);
        assertEquals("admin_users", table.name());
        assertFalse("customers".equalsIgnoreCase(table.name()));
    }

    @Test
    void supportsMultipleDomainAssignmentsViaAuthorizationEntities() {
        AdminUser adminUser = new AdminUser();
        adminUser.setEmail("multi-domain@cookies.dev");
        adminUser.setPassword("Secret123!");
        adminUser = adminUserRepository.saveAndFlush(adminUser);

        Domain domainA = createDomain("main-store");
        Domain domainB = createDomain("store-mx");
        Ability ability = createAbility("manage-customers");
        Permission permission = createPermission("customers:list", "customers", "list");

        UserDomainAbility assignmentA = new UserDomainAbility();
        assignmentA.setUserId(adminUser.getId());
        assignmentA.setDomain(domainA);
        assignmentA.setAbility(ability);
        assignmentA.setGranted(true);
        userDomainAbilityRepository.saveAndFlush(assignmentA);

        UserDomainAbility assignmentB = new UserDomainAbility();
        assignmentB.setUserId(adminUser.getId());
        assignmentB.setDomain(domainB);
        assignmentB.setAbility(ability);
        assignmentB.setGranted(true);
        userDomainAbilityRepository.saveAndFlush(assignmentB);

        UserDomainPermissionOverride allowOverride = new UserDomainPermissionOverride();
        allowOverride.setUserId(adminUser.getId());
        allowOverride.setDomain(domainA);
        allowOverride.setPermission(permission);
        allowOverride.setGranted(true);
        overrideRepository.saveAndFlush(allowOverride);

        List<UserDomainAbility> inDomainA = userDomainAbilityRepository.findByUserIdAndDomainCodeAndGrantedTrue(
            adminUser.getId(),
            "main-store"
        );
        List<UserDomainAbility> inDomainB = userDomainAbilityRepository.findByUserIdAndDomainCodeAndGrantedTrue(
            adminUser.getId(),
            "store-mx"
        );
        List<UserDomainPermissionOverride> overrides = overrideRepository.findByUserIdAndDomainCode(
            adminUser.getId(),
            "main-store"
        );

        assertEquals(1, inDomainA.size());
        assertEquals(1, inDomainB.size());
        assertEquals(1, overrides.size());
    }

    private Domain createDomain(String code) {
        Domain domain = new Domain();
        domain.setCode(code);
        domain.setName(code);
        return domainRepository.saveAndFlush(domain);
    }

    private Ability createAbility(String code) {
        Ability ability = new Ability();
        ability.setCode(code);
        ability.setName(code);
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
}
