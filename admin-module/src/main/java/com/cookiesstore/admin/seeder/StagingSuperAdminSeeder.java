package com.cookiesstore.admin.seeder;

import com.cookiesstore.admin.domain.AdminUser;
import com.cookiesstore.admin.repository.AdminUserRepository;
import com.cookiesstore.common.authorization.domain.Ability;
import com.cookiesstore.common.authorization.domain.Domain;
import com.cookiesstore.common.authorization.domain.UserDomainAbility;
import com.cookiesstore.common.authorization.repository.AbilityRepository;
import com.cookiesstore.common.authorization.repository.DomainRepository;
import com.cookiesstore.common.authorization.repository.UserDomainAbilityRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("staging")
public class StagingSuperAdminSeeder implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final DomainRepository domainRepository;
    private final AbilityRepository abilityRepository;
    private final UserDomainAbilityRepository userDomainAbilityRepository;

    @Value("${admin.bootstrap.super-admin.enabled:true}")
    private boolean enabled;

    @Value("${admin.bootstrap.super-admin.email:staging.admin@cookiesstore.local}")
    private String email;

    @Value("${admin.bootstrap.super-admin.password:ChangeMe123!}")
    private String password;

    @Value("${admin.bootstrap.super-admin.domains:main-store,example.test}")
    private String domainsCsv;

    public StagingSuperAdminSeeder(
        AdminUserRepository adminUserRepository,
        DomainRepository domainRepository,
        AbilityRepository abilityRepository,
        UserDomainAbilityRepository userDomainAbilityRepository
    ) {
        this.adminUserRepository = adminUserRepository;
        this.domainRepository = domainRepository;
        this.abilityRepository = abilityRepository;
        this.userDomainAbilityRepository = userDomainAbilityRepository;
    }

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }

        seed();
    }

    @Transactional
    public void seed() {
        Ability superAdminAbility = abilityRepository.findByCode("super-admin")
            .orElseThrow(() -> new IllegalStateException("Missing ability: super-admin"));

        AdminUser adminUser = adminUserRepository.findByEmail(email)
            .orElseGet(AdminUser::new);

        adminUser.setEmail(email);
        adminUser.setPassword(password);
        adminUser.setActive(true);
        adminUser = adminUserRepository.save(adminUser);

        List<String> domainCodes = Arrays.stream(domainsCsv.split(","))
            .map(String::trim)
            .filter(code -> !code.isBlank())
            .toList();

        for (String domainCode : domainCodes) {
            Domain domain = domainRepository.findByCode(domainCode)
                .orElseThrow(() -> new IllegalStateException("Missing domain: " + domainCode));

            UserDomainAbility grant = userDomainAbilityRepository
                .findByUserIdAndDomainCodeAndAbilityCode(adminUser.getId(), domainCode, "super-admin")
                .orElseGet(UserDomainAbility::new);

            grant.setUserId(adminUser.getId());
            grant.setDomain(domain);
            grant.setAbility(superAdminAbility);
            grant.setGranted(true);
            grant.setGrantedBy(adminUser.getId());

            userDomainAbilityRepository.save(grant);
        }
    }
}
