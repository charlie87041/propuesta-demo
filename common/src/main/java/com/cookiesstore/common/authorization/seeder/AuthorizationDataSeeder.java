package com.cookiesstore.common.authorization.seeder;

import com.cookiesstore.common.authorization.domain.Ability;
import com.cookiesstore.common.authorization.domain.Domain;
import com.cookiesstore.common.authorization.domain.Permission;
import com.cookiesstore.common.authorization.repository.AbilityRepository;
import com.cookiesstore.common.authorization.repository.DomainRepository;
import com.cookiesstore.common.authorization.repository.PermissionRepository;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@org.springframework.context.annotation.Profile("test")
public class AuthorizationDataSeeder implements CommandLineRunner {

    private final DomainRepository domainRepository;
    private final AbilityRepository abilityRepository;
    private final PermissionRepository permissionRepository;

    public AuthorizationDataSeeder(
        DomainRepository domainRepository,
        AbilityRepository abilityRepository,
        PermissionRepository permissionRepository
    ) {
        this.domainRepository = domainRepository;
        this.abilityRepository = abilityRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedDomains();
        seedPermissions();
        seedAbilitiesAndMappings();
    }

    private void seedDomains() {
        seedDomainIfMissing("main-store", "Main Cookie Store", "Primary e-commerce storefront");
        seedDomainIfMissing("example.test", "Example Test Domain", "Temporary domain for admin user-management tests");
    }

    private void seedDomainIfMissing(String code, String name, String description) {
        domainRepository.findByCode(code).orElseGet(() -> {
            Domain domain = new Domain();
            domain.setCode(code);
            domain.setName(name);
            domain.setDescription(description);
            return domainRepository.save(domain);
        });
    }

    private void seedPermissions() {
        List<String> codes = List.of(
            "products:list", "products:read", "products:create", "products:update", "products:delete", "products:toggle-status",
            "categories:list", "categories:read", "categories:create", "categories:update", "categories:delete",
            "inventory:view-stock", "inventory:update-stock", "inventory:view-alerts", "inventory:configure-alerts",
            "cart:view", "cart:add-item", "cart:update-item", "cart:remove-item", "cart:clear",
            "profile:read", "profile:update", "addresses:list", "addresses:create", "addresses:update", "addresses:delete",
            "customers:list", "customers:read", "customers:update", "customers:disable",
            "users:list", "users:read", "users:create", "users:update", "users:delete", "users:assign-ability", "users:revoke-ability", "users:override-permission",
            "orders:list-own", "orders:read-own", "orders:cancel-own", "orders:list", "orders:read", "orders:update-status",
            "orders:add-tracking", "orders:cancel", "orders:refund",
            "checkout:initiate", "checkout:complete",
            "payments:create-intent", "payments:confirm", "payments:view", "payments:refund",
            "reports:sales", "reports:inventory", "reports:customers", "reports:export",
            "settings:view", "settings:update", "audit:view", "*"
        );

        for (String code : codes) {
            if (permissionRepository.findByCode(code).isPresent()) {
                continue;
            }

            Permission permission = new Permission();
            permission.setCode(code);
            permission.setName(code);
            String[] parts = code.split(":");
            if (parts.length == 2) {
                permission.setResource(parts[0]);
                permission.setAction(parts[1]);
            } else {
                permission.setResource("*");
                permission.setAction("*");
            }
            permissionRepository.save(permission);
        }
    }

    private void seedAbilitiesAndMappings() {
        Map<String, Set<String>> mapping = new LinkedHashMap<>();
        mapping.put("browse-catalog", Set.of("products:list", "products:read", "categories:list", "categories:read"));
        mapping.put("manage-cart", Set.of("cart:view", "cart:add-item", "cart:update-item", "cart:remove-item", "cart:clear"));
        mapping.put("checkout", Set.of("checkout:initiate", "checkout:complete", "payments:create-intent", "payments:confirm"));
        mapping.put("manage-profile", Set.of("profile:read", "profile:update", "addresses:list", "addresses:create", "addresses:update", "addresses:delete"));
        mapping.put("view-orders", Set.of("orders:list-own", "orders:read-own", "orders:cancel-own"));
        mapping.put("view-inventory", Set.of("products:list", "products:read", "categories:list", "categories:read", "inventory:view-stock", "inventory:view-alerts"));
        mapping.put("manage-inventory", Set.of("products:list", "products:read", "products:create", "products:update", "products:delete", "products:toggle-status", "categories:list", "categories:read", "categories:create", "categories:update", "categories:delete", "inventory:view-stock", "inventory:update-stock", "inventory:view-alerts", "inventory:configure-alerts"));
        mapping.put("process-orders", Set.of("orders:list", "orders:read", "orders:update-status", "orders:add-tracking"));
        mapping.put("manage-orders", Set.of("orders:list", "orders:read", "orders:update-status", "orders:add-tracking", "orders:cancel", "orders:refund", "payments:view", "payments:refund"));
        mapping.put("manage-customers", Set.of("customers:list", "customers:read", "customers:update", "customers:disable", "orders:list", "orders:read"));
        mapping.put("manage-users", Set.of("users:list", "users:read", "users:create", "users:update", "users:delete", "users:assign-ability", "users:revoke-ability", "users:override-permission"));
        mapping.put("view-reports", Set.of("reports:sales", "reports:inventory", "reports:customers", "reports:export"));
        mapping.put("manage-settings", Set.of("settings:view", "settings:update", "audit:view"));
        mapping.put("super-admin", Set.of("*"));

        for (Map.Entry<String, Set<String>> entry : mapping.entrySet()) {
            String abilityCode = entry.getKey();
            Ability ability = abilityRepository.findByCode(abilityCode).orElseGet(() -> {
                Ability created = new Ability();
                created.setCode(abilityCode);
                created.setName(abilityCode);
                return abilityRepository.save(created);
            });

            Set<Permission> permissions = new LinkedHashSet<>();
            for (String permissionCode : entry.getValue()) {
                Permission permission = permissionRepository.findByCode(permissionCode)
                    .orElseThrow(() -> new IllegalStateException("Missing permission for seed mapping: " + permissionCode));
                permissions.add(permission);
            }

            ability.setPermissions(permissions);
            abilityRepository.save(ability);
        }
    }
}
