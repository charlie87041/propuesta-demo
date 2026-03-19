package com.cookiesstore.admin.service;

import com.cookiesstore.admin.domain.AdminUser;
import com.cookiesstore.admin.repository.AdminUserRepository;
import com.cookiesstore.common.auth.JwtTokenProvider;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminAuthenticationService {

    private final AdminUserRepository adminUserRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AdminAuthenticationService(AdminUserRepository adminUserRepository, JwtTokenProvider jwtTokenProvider) {
        this.adminUserRepository = adminUserRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public Optional<String> authenticate(String email, String rawPassword) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(rawPassword)) {
            return Optional.empty();
        }

        Optional<AdminUser> userOpt = adminUserRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        AdminUser user = userOpt.get();
        if (!user.isActive()) {
            return Optional.empty();
        }

        if (!BCrypt.checkpw(rawPassword, user.getPasswordHash())) {
            return Optional.empty();
        }

        return Optional.of(jwtTokenProvider.generateToken(user.getId()));
    }
}
