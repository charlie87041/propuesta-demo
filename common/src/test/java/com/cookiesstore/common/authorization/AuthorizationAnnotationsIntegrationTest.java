package com.cookiesstore.common.authorization;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cookiesstore.common.TestApplication;
import com.cookiesstore.common.auth.JwtTokenProvider;
import com.cookiesstore.common.authorization.annotation.RequiresAbility;
import com.cookiesstore.common.authorization.annotation.RequiresPermission;
import com.cookiesstore.common.authorization.domain.Ability;
import com.cookiesstore.common.authorization.domain.Domain;
import com.cookiesstore.common.authorization.domain.Permission;
import com.cookiesstore.common.authorization.domain.UserDomainAbility;
import com.cookiesstore.common.authorization.repository.AbilityRepository;
import com.cookiesstore.common.authorization.repository.DomainRepository;
import com.cookiesstore.common.authorization.repository.PermissionRepository;
import com.cookiesstore.common.authorization.repository.UserDomainAbilityRepository;
import com.cookiesstore.common.test.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(
    classes = {TestApplication.class, AuthorizationAnnotationsIntegrationTest.SecuredController.class},
    properties = {
        "security.jwt.secret=this-is-a-test-secret-key-with-at-least-32-bytes-long-1234567890",
        "security.jwt.expiration=PT1H"
    }
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthorizationAnnotationsIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private AbilityRepository abilityRepository;

    @Autowired
    private UserDomainAbilityRepository userDomainAbilityRepository;

    @Test
    void requiresPermissionAllowsAccessWithPermission() throws Exception {
        Domain domain = createDomain("ann-main");
        Permission permission = createPermission("products:list");
        Ability ability = createAbilityWithPermissions("browse-catalog", permission);
        grantAbility(501L, domain, ability);

        mockMvc.perform(get("/api/domains/ann-main/permission-check")
                .header(HttpHeaders.AUTHORIZATION, bearer(501L)))
            .andExpect(status().isOk());
    }

    @Test
    void requiresPermissionReturnsForbiddenWithoutPermission() throws Exception {
        createDomain("ann-no-access");

        mockMvc.perform(get("/api/domains/ann-no-access/permission-check")
                .header(HttpHeaders.AUTHORIZATION, bearer(502L)))
            .andExpect(status().isForbidden());
    }

    @Test
    void requiresPermissionReturnsForbiddenForWrongDomain() throws Exception {
        Domain domain = createDomain("ann-correct-domain");
        createDomain("ann-wrong-domain");
        Permission permission = createPermission("products:list");
        Ability ability = createAbilityWithPermissions("browse-catalog", permission);
        grantAbility(503L, domain, ability);

        mockMvc.perform(get("/api/domains/ann-wrong-domain/permission-check")
                .header(HttpHeaders.AUTHORIZATION, bearer(503L)))
            .andExpect(status().isForbidden());
    }

    @Test
    void requiresAbilityWorksCorrectly() throws Exception {
        Domain domain = createDomain("ann-ability");
        Ability ability = createAbilityWithPermissions("manage-orders", createPermission("orders:update-status"));
        grantAbility(504L, domain, ability);

        mockMvc.perform(get("/api/domains/ann-ability/ability-check")
                .header(HttpHeaders.AUTHORIZATION, bearer(504L)))
            .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedUserGetsUnauthorized() throws Exception {
        createDomain("ann-unauth");

        mockMvc.perform(get("/api/domains/ann-unauth/permission-check"))
            .andExpect(status().isUnauthorized());
    }

    private String bearer(Long userId) {
        return "Bearer " + jwtTokenProvider.generateToken(userId);
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

    private Ability createAbilityWithPermissions(String code, Permission permission) {
        Ability ability = new Ability();
        ability.setCode(code);
        ability.setName(code);
        ability.getPermissions().add(permission);
        return abilityRepository.save(ability);
    }

    private void grantAbility(Long userId, Domain domain, Ability ability) {
        UserDomainAbility grant = new UserDomainAbility();
        grant.setUserId(userId);
        grant.setDomain(domain);
        grant.setAbility(ability);
        grant.setGranted(true);
        userDomainAbilityRepository.save(grant);
    }

    @RestController
    @RequestMapping("/api/domains/{domainCode}")
    static class SecuredController {

        @GetMapping("/permission-check")
        @RequiresPermission("products:list")
        public String permissionCheck(@PathVariable String domainCode) {
            return "ok";
        }

        @GetMapping("/ability-check")
        @RequiresAbility("manage-orders")
        public String abilityCheck(@PathVariable String domainCode) {
            return "ok";
        }
    }
}
