package com.cookiesstore.pos.auth;

import com.cookiesstore.pos.domain.PosAdminUser;
import com.cookiesstore.pos.repository.PosAdminUserRepository;
import com.cookiesstore.pos.repository.PosSourceConfigRepository;
import com.cookiesstore.pos.repository.PosSourceRepository;
import com.cookiesstore.pos.repository.PosUserAssignmentRepository;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PosAuthenticationService {

    private final PosAdminUserRepository adminUserRepository;
    private final PosSourceRepository sourceRepository;
    private final PosSourceConfigRepository sourceConfigRepository;
    private final PosUserAssignmentRepository userAssignmentRepository;

    public PosAuthenticationService(
        PosAdminUserRepository adminUserRepository,
        PosSourceRepository sourceRepository,
        PosSourceConfigRepository sourceConfigRepository,
        PosUserAssignmentRepository userAssignmentRepository
    ) {
        this.adminUserRepository = adminUserRepository;
        this.sourceRepository = sourceRepository;
        this.sourceConfigRepository = sourceConfigRepository;
        this.userAssignmentRepository = userAssignmentRepository;
    }

    public PosAuthenticationResult authenticate(Long sourceId, String email, String rawPassword) {
        if (sourceId == null || !StringUtils.hasText(email) || !StringUtils.hasText(rawPassword)) {
            return PosAuthenticationResult.invalidCredentials();
        }

        var source = sourceRepository.findById(sourceId).orElse(null);
        if (source == null || !source.isActive()) {
            return PosAuthenticationResult.invalidCredentials();
        }

        var sourceConfig = sourceConfigRepository.findBySourceId(sourceId).orElse(null);
        if (sourceConfig == null || !sourceConfig.isPosEnabled()) {
            return PosAuthenticationResult.posDisabled();
        }

        PosAdminUser user = adminUserRepository.findByEmail(email.trim()).orElse(null);
        if (user == null || !user.isActive()) {
            return PosAuthenticationResult.invalidCredentials();
        }

        boolean assigned = userAssignmentRepository
            .findBySourceIdAndAdminUserIdAndActiveTrue(sourceId, user.getId())
            .isPresent();
        if (!assigned) {
            return PosAuthenticationResult.invalidCredentials();
        }

        if (!BCrypt.checkpw(rawPassword, user.getPasswordHash())) {
            return PosAuthenticationResult.invalidCredentials();
        }

        return PosAuthenticationResult.success(new PosPrincipal(user.getId(), sourceId, user.getEmail()));
    }
}
