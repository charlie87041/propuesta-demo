package com.cookiesstore.admin.seeder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cookiesstore.admin.domain.AdminUser;
import com.cookiesstore.admin.repository.AdminUserRepository;
import com.cookiesstore.common.authorization.domain.Ability;
import com.cookiesstore.common.authorization.domain.Domain;
import com.cookiesstore.common.authorization.domain.UserDomainAbility;
import com.cookiesstore.common.authorization.repository.AbilityRepository;
import com.cookiesstore.common.authorization.repository.DomainRepository;
import com.cookiesstore.common.authorization.repository.UserDomainAbilityRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = StagingSuperAdminSeederIntegrationTest.TestConfig.class)
@Transactional
@ActiveProfiles("staging")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:staging-seeder;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "admin.bootstrap.super-admin.enabled=false",
    "admin.bootstrap.super-admin.email=stage-admin@cookies.dev",
    "admin.bootstrap.super-admin.password=StageSecret123!",
    "admin.bootstrap.super-admin.domains=main-store,example.test"
})
class StagingSuperAdminSeederIntegrationTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = {AdminUser.class, Domain.class, Ability.class, UserDomainAbility.class})
    @EnableJpaRepositories(basePackageClasses = {
        AdminUserRepository.class,
        DomainRepository.class,
        AbilityRepository.class,
        UserDomainAbilityRepository.class
    })
    @Import(StagingSuperAdminSeeder.class)
    static class TestConfig {
    }

    @Autowired
    private StagingSuperAdminSeeder seeder;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private AbilityRepository abilityRepository;

    @Autowired
    private UserDomainAbilityRepository userDomainAbilityRepository;

    @BeforeEach
    void setupReferenceData() {
        ensureDomain("main-store");
        ensureDomain("example.test");
        ensureSuperAdminAbility();
    }

    @Test
    void createsSuperAdminAndGrantsAbilityInConfiguredDomains() {
        seeder.seed();

        AdminUser admin = adminUserRepository.findByEmail("stage-admin@cookies.dev").orElseThrow();
        assertTrue(admin.isActive());
        assertTrue(BCrypt.checkpw("StageSecret123!", admin.getPasswordHash()));

        List<UserDomainAbility> mainStore = userDomainAbilityRepository
            .findByUserIdAndDomainCodeAndGrantedTrue(admin.getId(), "main-store");
        List<UserDomainAbility> exampleTest = userDomainAbilityRepository
            .findByUserIdAndDomainCodeAndGrantedTrue(admin.getId(), "example.test");

        assertEquals(1, mainStore.size());
        assertEquals("super-admin", mainStore.get(0).getAbility().getCode());
        assertEquals(1, exampleTest.size());
        assertEquals("super-admin", exampleTest.get(0).getAbility().getCode());
    }

    @Test
    void seedIsIdempotent() {
        seeder.seed();
        seeder.seed();

        AdminUser admin = adminUserRepository.findByEmail("stage-admin@cookies.dev").orElseThrow();
        List<UserDomainAbility> mainStore = userDomainAbilityRepository
            .findByUserIdAndDomainCodeAndGrantedTrue(admin.getId(), "main-store");
        List<UserDomainAbility> exampleTest = userDomainAbilityRepository
            .findByUserIdAndDomainCodeAndGrantedTrue(admin.getId(), "example.test");

        assertEquals(1, mainStore.size());
        assertEquals(1, exampleTest.size());
    }

    private void ensureDomain(String code) {
        if (domainRepository.findByCode(code).isPresent()) {
            return;
        }

        Domain domain = new Domain();
        domain.setCode(code);
        domain.setName(code);
        domainRepository.saveAndFlush(domain);
    }

    private void ensureSuperAdminAbility() {
        if (abilityRepository.findByCode("super-admin").isPresent()) {
            return;
        }

        Ability ability = new Ability();
        ability.setCode("super-admin");
        ability.setName("super-admin");
        abilityRepository.saveAndFlush(ability);
    }
}
