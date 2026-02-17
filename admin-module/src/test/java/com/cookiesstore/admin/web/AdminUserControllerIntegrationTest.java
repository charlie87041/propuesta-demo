package com.cookiesstore.admin.web;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cookiesstore.admin.domain.AdminUser;
import com.cookiesstore.admin.repository.AdminUserRepository;
import com.cookiesstore.admin.service.AdminAbilityAssignmentService;
import com.cookiesstore.admin.service.AdminUserService;
import com.cookiesstore.common.auth.JwtTokenProvider;
import com.cookiesstore.common.authorization.domain.Ability;
import com.cookiesstore.common.authorization.domain.Domain;
import com.cookiesstore.common.authorization.domain.Permission;
import com.cookiesstore.common.authorization.domain.UserDomainAbility;
import com.cookiesstore.common.authorization.domain.UserDomainPermissionOverride;
import com.cookiesstore.common.authorization.evaluator.AuthorizationAspect;
import com.cookiesstore.common.authorization.evaluator.DomainAuthorizationEvaluator;
import com.cookiesstore.common.authorization.repository.AbilityRepository;
import com.cookiesstore.common.authorization.repository.DomainRepository;
import com.cookiesstore.common.authorization.repository.PermissionRepository;
import com.cookiesstore.common.authorization.repository.UserDomainAbilityRepository;
import com.cookiesstore.common.authorization.repository.UserDomainPermissionOverrideRepository;
import com.cookiesstore.common.authorization.service.DomainAuthorizationService;
import com.cookiesstore.common.config.CommonConfiguration;
import com.cookiesstore.common.security.JwtAuthenticationFilter;
import com.cookiesstore.common.security.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    classes = AdminUserControllerIntegrationTest.TestConfig.class,
    properties = {
        "security.jwt.secret=this-is-a-test-secret-key-with-at-least-32-bytes-long-1234567890",
        "security.jwt.expiration=PT1H",
        "spring.datasource.url=jdbc:h2:mem:admin86;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    }
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminUserControllerIntegrationTest {

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
    @Import({
        CommonConfiguration.class,
        JwtTokenProvider.class,
        JwtAuthenticationFilter.class,
        SecurityConfig.class,
        DomainAuthorizationService.class,
        DomainAuthorizationEvaluator.class,
        AuthorizationAspect.class,
        AdminAbilityAssignmentService.class,
        AdminUserService.class,
        AdminUserController.class
    })
    static class TestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AdminUserRepository adminUserRepository;

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
    void listUsersReturnsUnauthorizedWhenNoAuthentication() throws Exception {
        createDomain("example.test");

        mockMvc.perform(get("/api/domains/example.test/admin/users"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void listUsersReturnsForbiddenForUserWithoutManageCustomersAbility() throws Exception {
        createDomain("example.test");

        mockMvc.perform(get("/api/domains/example.test/admin/users")
                .header(HttpHeaders.AUTHORIZATION, bearer(5001L)))
            .andExpect(status().isForbidden());
    }

    @Test
    void listUsersAllowsAdminWithManageCustomersAbility() throws Exception {
        Domain domain = createDomain("example.test");
        Ability manageCustomers = createAbility("manage-users");

        AdminUser listedUser = createAdmin("listed@cookies.dev");
        grantAbility(listedUser.getId(), domain, manageCustomers);

        Long actorId = 5002L;
        grantAbility(actorId, domain, manageCustomers);

        mockMvc.perform(get("/api/domains/example.test/admin/users")
                .header(HttpHeaders.AUTHORIZATION, bearer(actorId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].email").value("listed@cookies.dev"));
    }

    @Test
    void listUsersEnforcesDomainIsolation() throws Exception {
        Domain actorDomain = createDomain("domain-a");
        createDomain("domain-b");
        Ability manageCustomers = createAbility("manage-users");

        Long actorId = 5003L;
        grantAbility(actorId, actorDomain, manageCustomers);

        mockMvc.perform(get("/api/domains/domain-b/admin/users")
                .header(HttpHeaders.AUTHORIZATION, bearer(actorId)))
            .andExpect(status().isForbidden());
    }

    @Test
    void createUserEndpointWorks() throws Exception {
        Domain domain = createDomain("example.test");
        Ability manageCustomers = createAbility("manage-users");

        Long actorId = 5004L;
        grantAbility(actorId, domain, manageCustomers);

        String payload = objectMapper.writeValueAsString(new CreateUserRequest("create@cookies.dev", "Secret123!"));

        mockMvc.perform(post("/api/domains/example.test/admin/users")
                .header(HttpHeaders.AUTHORIZATION, bearer(actorId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.email").value("create@cookies.dev"));
    }

    @Test
    void getUpdateDeleteUserEndpointsWork() throws Exception {
        Domain domain = createDomain("example.test");
        Ability manageCustomers = createAbility("manage-users");

        Long actorId = 5005L;
        grantAbility(actorId, domain, manageCustomers);

        AdminUser target = createAdmin("target@cookies.dev");
        grantAbility(target.getId(), domain, manageCustomers);

        mockMvc.perform(get("/api/domains/example.test/admin/users/{id}", target.getId())
                .header(HttpHeaders.AUTHORIZATION, bearer(actorId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value("target@cookies.dev"));

        String updatePayload = objectMapper.writeValueAsString(new UpdateUserRequest("updated@cookies.dev", "NewSecret123!"));

        mockMvc.perform(put("/api/domains/example.test/admin/users/{id}", target.getId())
                .header(HttpHeaders.AUTHORIZATION, bearer(actorId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value("updated@cookies.dev"));

        mockMvc.perform(delete("/api/domains/example.test/admin/users/{id}", target.getId())
                .header(HttpHeaders.AUTHORIZATION, bearer(actorId)))
            .andExpect(status().isNoContent());
    }

    @Test
    void assignAndRevokeAbilityEndpointsWorkForSuperAdmin() throws Exception {
        Domain domain = createDomain("example.test");
        Ability superAdmin = createAbility("super-admin");
        Ability manageOrders = createAbility("manage-orders");

        Long actorId = 5006L;
        grantAbility(actorId, domain, superAdmin);
        grantAbility(actorId, domain, manageOrders);

        AdminUser target = createAdmin("ability-target@cookies.dev");

        mockMvc.perform(post("/api/domains/example.test/admin/users/{id}/abilities", target.getId())
                .header(HttpHeaders.AUTHORIZATION, bearer(actorId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"abilityId\":" + manageOrders.getId() + "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(delete("/api/domains/example.test/admin/users/{id}/abilities/{abilityId}", target.getId(), manageOrders.getId())
                .header(HttpHeaders.AUTHORIZATION, bearer(actorId)))
            .andExpect(status().isNoContent());
    }

    @Test
    void assignAbilityRequiresSuperAdmin() throws Exception {
        Domain domain = createDomain("example.test");
        Ability manageCustomers = createAbility("manage-users");
        Ability manageOrders = createAbility("manage-orders");

        Long actorId = 5007L;
        grantAbility(actorId, domain, manageCustomers);
        grantAbility(actorId, domain, manageOrders);

        AdminUser target = createAdmin("forbidden-assign@cookies.dev");

        mockMvc.perform(post("/api/domains/example.test/admin/users/{id}/abilities", target.getId())
                .header(HttpHeaders.AUTHORIZATION, bearer(actorId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"abilityId\":" + manageOrders.getId() + "}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void assignAbilityRejectsEscalationWhenActorDoesNotPossessTargetAbility() throws Exception {
        Domain domain = createDomain("example.test");
        Ability superAdmin = createAbility("super-admin");
        Ability manageOrders = createAbility("manage-orders");

        Long actorId = 5008L;
        grantAbility(actorId, domain, superAdmin);

        AdminUser target = createAdmin("escalation@cookies.dev");

        mockMvc.perform(post("/api/domains/example.test/admin/users/{id}/abilities", target.getId())
                .header(HttpHeaders.AUTHORIZATION, bearer(actorId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"abilityId\":" + manageOrders.getId() + "}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void permissionOverrideEndpointWorks() throws Exception {
        Domain domain = createDomain("example.test");
        Ability manageCustomers = createAbility("manage-users");
        Permission permission = createPermission("users:override-permission", "users", "override-permission");

        Long actorId = 5009L;
        grantAbility(actorId, domain, manageCustomers);

        AdminUser target = createAdmin("override-target@cookies.dev");

        mockMvc.perform(post("/api/domains/example.test/admin/users/{id}/permissions/override", target.getId())
                .header(HttpHeaders.AUTHORIZATION, bearer(actorId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"permissionCode\":\"" + permission.getCode() + "\",\"granted\":true}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        assertTrue(overrideRepository.findByUserIdAndDomainCodeAndPermissionCode(target.getId(), "example.test", permission.getCode()).isPresent());
    }

    private String bearer(Long userId) {
        return "Bearer " + jwtTokenProvider.generateToken(userId);
    }

    private Domain createDomain(String code) {
        Domain domain = new Domain();
        domain.setCode(code);
        domain.setName(code);
        return domainRepository.saveAndFlush(domain);
    }

    private Permission createPermission(String code, String resource, String action) {
        Permission permission = new Permission();
        permission.setCode(code);
        permission.setName(code);
        permission.setResource(resource);
        permission.setAction(action);
        return permissionRepository.saveAndFlush(permission);
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

    private void grantAbility(Long userId, Domain domain, Ability ability) {
        UserDomainAbility grant = new UserDomainAbility();
        grant.setUserId(userId);
        grant.setDomain(domain);
        grant.setAbility(ability);
        grant.setGranted(true);
        userDomainAbilityRepository.saveAndFlush(grant);
    }

    private AdminUser createAdmin(String email) {
        AdminUser adminUser = new AdminUser();
        adminUser.setEmail(email);
        adminUser.setPassword("Secret123!");
        return adminUserRepository.saveAndFlush(adminUser);
    }

    private record CreateUserRequest(String email, String password) {
    }

    private record UpdateUserRequest(String email, String password) {
    }
}
