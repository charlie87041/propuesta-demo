package com.cookiesstore.common.authorization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cookiesstore.common.TestApplication;
import com.cookiesstore.common.authorization.domain.Ability;
import com.cookiesstore.common.authorization.repository.AbilityRepository;
import com.cookiesstore.common.authorization.repository.DomainRepository;
import com.cookiesstore.common.authorization.repository.PermissionRepository;
import com.cookiesstore.common.test.AbstractIntegrationTest;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@Transactional
class AuthorizationDataSeederIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private AbilityRepository abilityRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Test
    void mainStoreDomainExistsAfterSeed() {
        assertTrue(domainRepository.findByCode("main-store").isPresent());
    }

    @Test
    void allDefinedAbilitiesExistAfterSeed() {
        assertEquals(13L, abilityRepository.count());

        Set<String> abilityCodes = abilityRepository.findAll().stream().map(Ability::getCode).collect(Collectors.toSet());
        assertTrue(abilityCodes.contains("browse-catalog"));
        assertTrue(abilityCodes.contains("manage-cart"));
        assertTrue(abilityCodes.contains("checkout"));
        assertTrue(abilityCodes.contains("manage-profile"));
        assertTrue(abilityCodes.contains("view-orders"));
        assertTrue(abilityCodes.contains("view-inventory"));
        assertTrue(abilityCodes.contains("manage-inventory"));
        assertTrue(abilityCodes.contains("process-orders"));
        assertTrue(abilityCodes.contains("manage-orders"));
        assertTrue(abilityCodes.contains("manage-customers"));
        assertTrue(abilityCodes.contains("view-reports"));
        assertTrue(abilityCodes.contains("manage-settings"));
        assertTrue(abilityCodes.contains("super-admin"));
    }

    @Test
    void allDefinedPermissionsExistAfterSeed() {
        assertTrue(permissionRepository.count() >= 40L);
        assertTrue(permissionRepository.findByCode("products:list").isPresent());
        assertTrue(permissionRepository.findByCode("orders:update-status").isPresent());
        assertTrue(permissionRepository.findByCode("payments:refund").isPresent());
        assertTrue(permissionRepository.findByCode("*").isPresent());
    }

    @Test
    void abilityPermissionMappingsAreCorrect() {
        Ability browseCatalog = abilityRepository.findByCode("browse-catalog").orElseThrow();
        Set<String> browsePermissions = browseCatalog.getPermissions().stream().map(p -> p.getCode()).collect(Collectors.toSet());
        assertTrue(browsePermissions.contains("products:list"));
        assertTrue(browsePermissions.contains("categories:list"));

        Ability manageInventory = abilityRepository.findByCode("manage-inventory").orElseThrow();
        Set<String> inventoryPermissions = manageInventory.getPermissions().stream().map(p -> p.getCode()).collect(Collectors.toSet());
        assertTrue(inventoryPermissions.contains("products:create"));
        assertTrue(inventoryPermissions.contains("inventory:update-stock"));

        Ability superAdmin = abilityRepository.findByCode("super-admin").orElseThrow();
        Set<String> superAdminPermissions = superAdmin.getPermissions().stream().map(p -> p.getCode()).collect(Collectors.toSet());
        assertTrue(superAdminPermissions.contains("*"));
    }
}
