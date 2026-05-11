package com.cookiesstore.infra.auth.service;

import com.cookiesstore.infra.auth.config.InfraAuthSeedProperties;
import com.cookiesstore.infra.auth.domain.InfraUser;
import com.cookiesstore.infra.auth.repository.InfraUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InfraUserSeeder implements ApplicationRunner {

    private final InfraUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final InfraAuthSeedProperties seedProperties;

    public InfraUserSeeder(
        InfraUserRepository userRepository,
        PasswordEncoder passwordEncoder,
        InfraAuthSeedProperties seedProperties
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedProperties = seedProperties;
    }

    @Override
    @Transactional
    public void run(org.springframework.boot.ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }

        String username = seedProperties.username() == null || seedProperties.username().isBlank()
            ? "admin"
            : seedProperties.username().trim();
        String password = seedProperties.password() == null || seedProperties.password().isBlank()
            ? "change-me"
            : seedProperties.password();
        String displayName = seedProperties.displayName() == null || seedProperties.displayName().isBlank()
            ? "Infra Admin"
            : seedProperties.displayName().trim();

        InfraUser user = new InfraUser(username, passwordEncoder.encode(password), displayName);
        userRepository.save(user);
    }
}
