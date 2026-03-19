package com.cookiesstore.common.authorization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.cookiesstore.common.test.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
class AuthorizationEntitiesIntegrationTest extends AbstractIntegrationTest {

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
    void createsDomainSuccessfully() {
        Domain domain = new Domain();
        domain.setCode("main-store");
        domain.setName("Main Store");
        domain.setDescription("Primary storefront");

        Domain saved = domainRepository.save(domain);

        assertNotNull(saved.getId());
        assertEquals("main-store", saved.getCode());
        assertTrue(saved.isActive());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void createsPermissionWithResourceAndAction() {
        Permission permission = new Permission();
        permission.setCode("products:list");
        permission.setName("List Products");
        permission.setResource("products");
        permission.setAction("list");

        Permission saved = permissionRepository.save(permission);

        assertNotNull(saved.getId());
        assertEquals("products", saved.getResource());
        assertEquals("list", saved.getAction());
    }

    @Test
    void createsAbilityWithPermissions() {
        Permission p1 = new Permission();
        p1.setCode("products:read");
        p1.setName("Read Products");
        p1.setResource("products");
        p1.setAction("read");
        permissionRepository.save(p1);

        Permission p2 = new Permission();
        p2.setCode("products:update");
        p2.setName("Update Products");
        p2.setResource("products");
        p2.setAction("update");
        permissionRepository.save(p2);

        Ability ability = new Ability();
        ability.setCode("manage-inventory");
        ability.setName("Manage Inventory");
        ability.getPermissions().add(p1);
        ability.getPermissions().add(p2);

        Ability saved = abilityRepository.save(ability);

        assertNotNull(saved.getId());
        assertEquals(2, saved.getPermissions().size());
    }

    @Test
    void grantsAndRevokesUserDomainAbility() {
        Domain domain = createDomain("store-a");
        Ability ability = createAbility("manage-orders");

        UserDomainAbility grant = new UserDomainAbility();
        grant.setUserId(101L);
        grant.setDomain(domain);
        grant.setAbility(ability);
        grant.setGranted(true);

        UserDomainAbility saved = userDomainAbilityRepository.save(grant);
        assertNotNull(saved.getId());
        assertTrue(saved.isGranted());

        saved.setGranted(false);
        UserDomainAbility revoked = userDomainAbilityRepository.save(saved);
        assertFalse(revoked.isGranted());
    }

    @Test
    void createsUserDomainPermissionOverride() {
        Domain domain = createDomain("store-b");
        Permission permission = createPermission("orders:refund", "orders", "refund");

        UserDomainPermissionOverride override = new UserDomainPermissionOverride();
        override.setUserId(202L);
        override.setDomain(domain);
        override.setPermission(permission);
        override.setGranted(false);
        override.setGrantedBy(1L);

        UserDomainPermissionOverride saved = overrideRepository.save(override);

        assertNotNull(saved.getId());
        assertFalse(saved.isGranted());
        assertEquals(202L, saved.getUserId());
    }

    @Test
    void enforcesUniqueConstraintForUserDomainAbility() {
        Domain domain = createDomain("store-c");
        Ability ability = createAbility("manage-customers");

        UserDomainAbility first = new UserDomainAbility();
        first.setUserId(303L);
        first.setDomain(domain);
        first.setAbility(ability);
        first.setGranted(true);
        userDomainAbilityRepository.saveAndFlush(first);

        UserDomainAbility duplicated = new UserDomainAbility();
        duplicated.setUserId(303L);
        duplicated.setDomain(domain);
        duplicated.setAbility(ability);
        duplicated.setGranted(true);

        assertThrows(DataIntegrityViolationException.class, () -> userDomainAbilityRepository.saveAndFlush(duplicated));
    }

    private Domain createDomain(String code) {
        Domain domain = new Domain();
        domain.setCode(code);
        domain.setName(code);
        return domainRepository.save(domain);
    }

    private Ability createAbility(String code) {
        Ability ability = new Ability();
        ability.setCode(code);
        ability.setName(code);
        return abilityRepository.save(ability);
    }

    private Permission createPermission(String code, String resource, String action) {
        Permission permission = new Permission();
        permission.setCode(code);
        permission.setName(code);
        permission.setResource(resource);
        permission.setAction(action);
        return permissionRepository.save(permission);
    }
}
