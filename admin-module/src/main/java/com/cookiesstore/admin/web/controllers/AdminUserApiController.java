package com.cookiesstore.admin.web.controllers;

import com.cookiesstore.admin.domain.AdminUser;
import com.cookiesstore.admin.service.AdminUserService;
import com.cookiesstore.admin.web.dto.users.AdminUserResponse;
import com.cookiesstore.admin.web.dto.users.AssignAbilityRequest;
import com.cookiesstore.admin.web.dto.users.CreateAdminUserRequest;
import com.cookiesstore.admin.web.dto.users.PermissionOverrideRequest;
import com.cookiesstore.admin.web.dto.users.UpdateAdminUserRequest;
import com.cookiesstore.common.api.ApiResponse;
import com.cookiesstore.common.authorization.annotation.RequiresAbility;
import com.cookiesstore.common.authorization.domain.Ability;
import com.cookiesstore.common.authorization.repository.AbilityRepository;
import com.cookiesstore.common.authorization.service.DomainAuthorizationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminUserApiController {

    private final AdminUserService adminUserService;
    private final DomainAuthorizationService domainAuthorizationService;
    private final AbilityRepository abilityRepository;

    public AdminUserApiController(
        AdminUserService adminUserService,
        DomainAuthorizationService domainAuthorizationService,
        AbilityRepository abilityRepository
    ) {
        this.adminUserService = adminUserService;
        this.domainAuthorizationService = domainAuthorizationService;
        this.abilityRepository = abilityRepository;
    }

    @GetMapping("/api/domains/{domainCode}/admin/users")
    @RequiresAbility("manage-users")
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> listUsers(@PathVariable("domainCode") String domainCode) {
        List<AdminUserResponse> users = adminUserService.listAdminUsersByDomain(domainCode)
            .stream()
            .map(AdminUserResponse::from)
            .toList();

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PostMapping("/api/domains/{domainCode}/admin/users")
    @RequiresAbility("manage-users")
    public ResponseEntity<ApiResponse<AdminUserResponse>> createUser(
        @PathVariable("domainCode") String domainCode,
        @Valid @RequestBody CreateAdminUserRequest request
    ) {
        Long actorUserId = currentUserId();
        String actorDomainCode = adminUserService.resolveActorDomainCode(actorUserId);
        if (!actorDomainCode.equals(domainCode)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Domain mismatch");
        }
        AdminUser created = adminUserService.createAdminUserWithRole(actorUserId, request.email(), request.password(), request.roleCode());
        return ResponseEntity.status(201).body(ApiResponse.success(AdminUserResponse.from(created)));
    }

    @GetMapping("/api/domains/{domainCode}/admin/users/{id}")
    @RequiresAbility("manage-users")
    public ResponseEntity<ApiResponse<AdminUserResponse>> getUser(@PathVariable("domainCode") String domainCode, @PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(AdminUserResponse.from(adminUserService.getAdminUser(id))));
    }

    @PutMapping("/api/domains/{domainCode}/admin/users/{id}")
    @RequiresAbility("manage-users")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateUser(
        @PathVariable("domainCode") String domainCode,
        @PathVariable("id") Long id,
        @Valid @RequestBody UpdateAdminUserRequest request
    ) {
        Long actorUserId = currentUserId();
        String actorDomainCode = adminUserService.resolveActorDomainCode(actorUserId);
        if (!actorDomainCode.equals(domainCode)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Domain mismatch");
        }
        AdminUser updated = adminUserService.updateAdminUserWithRole(actorUserId, id, request.email(), request.password(), request.roleCode());
        return ResponseEntity.ok(ApiResponse.success(AdminUserResponse.from(updated)));
    }

    @DeleteMapping("/api/domains/{domainCode}/admin/users/{id}")
    @RequiresAbility("manage-users")
    public ResponseEntity<Void> deactivateUser(@PathVariable("domainCode") String domainCode, @PathVariable("id") Long id) {
        adminUserService.deactivateAdminUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/domains/{domainCode}/admin/users/{id}/abilities")
    @RequiresAbility("super-admin")
    public ResponseEntity<ApiResponse<Void>> assignAbility(
        @PathVariable("domainCode") String domainCode,
        @PathVariable("id") Long id,
        @Valid @RequestBody AssignAbilityRequest request
    ) {
        Long actorUserId = currentUserId();
        Ability ability = abilityRepository.findById(request.abilityId())
            .orElseThrow(() -> new IllegalArgumentException("Ability not found: " + request.abilityId()));

        if (!domainAuthorizationService.hasAbility(actorUserId, domainCode, ability.getCode())
            && !"super-admin".equals(ability.getCode())) {
            throw new AccessDeniedException("Cannot assign ability you do not possess");
        }

        adminUserService.assignAbility(actorUserId, id, domainCode, ability.getCode());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/api/domains/{domainCode}/admin/users/{id}/abilities/{abilityId}")
    @RequiresAbility("super-admin")
    public ResponseEntity<Void> revokeAbility(
        @PathVariable("domainCode") String domainCode,
        @PathVariable("id") Long id,
        @PathVariable("abilityId") Long abilityId
    ) {
        Long actorUserId = currentUserId();
        Ability ability = abilityRepository.findById(abilityId)
            .orElseThrow(() -> new IllegalArgumentException("Ability not found: " + abilityId));

        if (!domainAuthorizationService.hasAbility(actorUserId, domainCode, ability.getCode())) {
            throw new AccessDeniedException("Cannot revoke ability you do not possess");
        }

        adminUserService.revokeAbility(actorUserId, id, domainCode, ability.getCode());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/domains/{domainCode}/admin/users/{id}/permissions/override")
    @RequiresAbility("manage-users")
    public ResponseEntity<ApiResponse<Void>> assignPermissionOverride(
        @PathVariable("domainCode") String domainCode,
        @PathVariable("id") Long id,
        @Valid @RequestBody PermissionOverrideRequest request
    ) {
        adminUserService.assignPermissionOverride(currentUserId(), id, domainCode, request.permissionCode(), request.granted());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AccessDeniedException("Unauthenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Long userId) {
            return userId;
        }
        if (principal instanceof String textPrincipal) {
            return Long.parseLong(textPrincipal);
        }

        throw new AccessDeniedException("Invalid authentication principal");
    }
}
