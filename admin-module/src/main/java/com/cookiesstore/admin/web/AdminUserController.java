package com.cookiesstore.admin.web;

import com.cookiesstore.admin.domain.AdminUser;
import com.cookiesstore.admin.service.AdminUserService;
import com.cookiesstore.admin.web.dto.AdminUserResponse;
import com.cookiesstore.admin.web.dto.AssignAbilityRequest;
import com.cookiesstore.admin.web.dto.CreateAdminUserRequest;
import com.cookiesstore.admin.web.dto.PermissionOverrideRequest;
import com.cookiesstore.admin.web.dto.UpdateAdminUserRequest;
import com.cookiesstore.common.api.ApiResponse;
import com.cookiesstore.common.authorization.annotation.RequiresAbility;
import com.cookiesstore.common.authorization.domain.Ability;
import com.cookiesstore.common.authorization.domain.Permission;
import com.cookiesstore.common.authorization.repository.AbilityRepository;
import com.cookiesstore.common.authorization.service.DomainAuthorizationService;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final DomainAuthorizationService domainAuthorizationService;
    private final AbilityRepository abilityRepository;

    public AdminUserController(
        AdminUserService adminUserService,
        DomainAuthorizationService domainAuthorizationService,
        AbilityRepository abilityRepository
    ) {
        this.adminUserService = adminUserService;
        this.domainAuthorizationService = domainAuthorizationService;
        this.abilityRepository = abilityRepository;
    }

    @GetMapping("/admin/users")
    public String usersList(Model model) {
        Long actorUserId = currentUserId();
        String domainCode = adminUserService.resolveActorDomainCode(actorUserId);

        List<AdminUser> users = adminUserService.listAdminUsersByDomain(domainCode);
        Map<Long, String> userRoles = new LinkedHashMap<>();
        for (AdminUser user : users) {
            userRoles.put(user.getId(), adminUserService.findPrimaryRoleCode(user.getId(), domainCode));
        }

        model.addAttribute("users", users);
        model.addAttribute("domainCode", domainCode);
        model.addAttribute("userRoles", userRoles);
        model.addAttribute("pageTitle", "Admin User Management");
        return "backoffice/users/index";
    }

    @GetMapping("/admin/users/new")
    public String createUserView(Model model) {
        Long actorUserId = currentUserId();
        String domainCode = adminUserService.resolveActorDomainCode(actorUserId);
        populateCreateFormModel(model, domainCode, null);
        return "backoffice/users/form";
    }

    @PostMapping("/admin/users")
    public String createUser(
        @RequestParam String email,
        @RequestParam String password,
        @RequestParam String roleCode,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        Long actorUserId = currentUserId();
        String domainCode = adminUserService.resolveActorDomainCode(actorUserId);

        populateCreateFormModel(model, domainCode, roleCode);
        model.addAttribute("email", email);

        boolean hasErrors = validateForm(email, password, roleCode, false, model);
        if (hasErrors) {
            return "backoffice/users/form";
        }

        try {
            adminUserService.createAdminUserWithRole(actorUserId, email, password, roleCode);
            redirectAttributes.addFlashAttribute("successMessage", "Admin user created successfully.");
            return "redirect:/admin/users";
        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "backoffice/users/form";
        }
    }

    @GetMapping("/admin/users/{userId}/edit")
    public String editUserView(@PathVariable Long userId, Model model, RedirectAttributes redirectAttributes) {
        try {
            AdminUser user = adminUserService.getAdminUser(userId);
            String domainCode = adminUserService.resolveUserDomainCode(userId);
            String selectedRoleCode = adminUserService.findPrimaryRoleCode(userId, domainCode);
            populateEditFormModel(model, userId, domainCode, selectedRoleCode);
            model.addAttribute("email", user.getEmail());
            return "backoffice/users/form";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/admin/users/{userId}")
    public String updateUser(
        @PathVariable Long userId,
        @RequestParam String email,
        @RequestParam(required = false) String password,
        @RequestParam String roleCode,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        Long actorUserId = currentUserId();
        String domainCode;
        try {
            domainCode = adminUserService.resolveUserDomainCode(userId);
        } catch (IllegalArgumentException ex) {
            domainCode = adminUserService.resolveActorDomainCode(actorUserId);
        }

        populateEditFormModel(model, userId, domainCode, roleCode);
        model.addAttribute("email", email);

        boolean hasErrors = validateForm(email, password, roleCode, true, model);
        if (hasErrors) {
            return "backoffice/users/form";
        }

        try {
            adminUserService.updateAdminUserWithRole(actorUserId, userId, email, password, roleCode);
            redirectAttributes.addFlashAttribute("successMessage", "Admin user updated successfully.");
            return "redirect:/admin/users";
        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "backoffice/users/form";
        }
    }

    @PostMapping("/admin/users/{userId}/deactivate")
    public String deactivateUserFromView(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            adminUserService.deactivateAdminUser(userId);
            redirectAttributes.addFlashAttribute("successMessage", "Admin user deactivated.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @ResponseBody
    @GetMapping("/api/domains/{domainCode}/admin/users")
    @RequiresAbility("manage-users")
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> listUsers(@PathVariable String domainCode) {
        List<AdminUserResponse> users = adminUserService.listAdminUsersByDomain(domainCode)
            .stream()
            .map(AdminUserResponse::from)
            .toList();

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @ResponseBody
    @PostMapping("/api/domains/{domainCode}/admin/users")
    @RequiresAbility("manage-users")
    public ResponseEntity<ApiResponse<AdminUserResponse>> createUser(
        @PathVariable String domainCode,
        @Valid @RequestBody CreateAdminUserRequest request
    ) {
        AdminUser created = adminUserService.createAdminUser(request.email(), request.password());
        return ResponseEntity.status(201).body(ApiResponse.success(AdminUserResponse.from(created)));
    }

    @ResponseBody
    @GetMapping("/api/domains/{domainCode}/admin/users/{id}")
    @RequiresAbility("manage-users")
    public ResponseEntity<ApiResponse<AdminUserResponse>> getUser(@PathVariable String domainCode, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(AdminUserResponse.from(adminUserService.getAdminUser(id))));
    }

    @ResponseBody
    @PutMapping("/api/domains/{domainCode}/admin/users/{id}")
    @RequiresAbility("manage-users")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateUser(
        @PathVariable String domainCode,
        @PathVariable Long id,
        @Valid @RequestBody UpdateAdminUserRequest request
    ) {
        AdminUser updated = adminUserService.updateAdminUser(id, request.email(), request.password());
        return ResponseEntity.ok(ApiResponse.success(AdminUserResponse.from(updated)));
    }

    @ResponseBody
    @DeleteMapping("/api/domains/{domainCode}/admin/users/{id}")
    @RequiresAbility("manage-users")
    public ResponseEntity<Void> deactivateUser(@PathVariable String domainCode, @PathVariable Long id) {
        adminUserService.deactivateAdminUser(id);
        return ResponseEntity.noContent().build();
    }

    @ResponseBody
    @PostMapping("/api/domains/{domainCode}/admin/users/{id}/abilities")
    @RequiresAbility("super-admin")
    public ResponseEntity<ApiResponse<Void>> assignAbility(
        @PathVariable String domainCode,
        @PathVariable Long id,
        @Valid @RequestBody AssignAbilityRequest request
    ) {
        Long actorUserId = currentUserId();
        Ability ability = abilityRepository.findById(request.abilityId())
            .orElseThrow(() -> new IllegalArgumentException("Ability not found: " + request.abilityId()));

        if (!domainAuthorizationService.hasAbility(actorUserId, domainCode, ability.getCode()) && !"super-admin".equals(ability.getCode())) {
            throw new AccessDeniedException("Cannot assign ability you do not possess");
        }

        adminUserService.assignAbility(actorUserId, id, domainCode, ability.getCode());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @ResponseBody
    @DeleteMapping("/api/domains/{domainCode}/admin/users/{id}/abilities/{abilityId}")
    @RequiresAbility("super-admin")
    public ResponseEntity<Void> revokeAbility(
        @PathVariable String domainCode,
        @PathVariable Long id,
        @PathVariable Long abilityId
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

    @ResponseBody
    @PostMapping("/api/domains/{domainCode}/admin/users/{id}/permissions/override")
    @RequiresAbility("manage-users")
    public ResponseEntity<ApiResponse<Void>> assignPermissionOverride(
        @PathVariable String domainCode,
        @PathVariable Long id,
        @Valid @RequestBody PermissionOverrideRequest request
    ) {
        adminUserService.assignPermissionOverride(currentUserId(), id, domainCode, request.permissionCode(), request.granted());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private boolean validateForm(String email, String password, String roleCode, boolean isEdit, Model model) {
        boolean hasErrors = false;

        if (!StringUtils.hasText(email)) {
            model.addAttribute("emailError", "Email is required.");
            hasErrors = true;
        } else if (!email.contains("@")) {
            model.addAttribute("emailError", "Email format is invalid.");
            hasErrors = true;
        }

        if (!isEdit && !StringUtils.hasText(password)) {
            model.addAttribute("passwordError", "Password is required.");
            hasErrors = true;
        }

        if (!StringUtils.hasText(roleCode)) {
            model.addAttribute("roleError", "Role is required.");
            hasErrors = true;
        }

        return hasErrors;
    }

    private void populateCreateFormModel(Model model, String domainCode, String selectedRoleCode) {
        model.addAttribute("pageTitle", "Crear usuario admin");
        model.addAttribute("isEdit", false);
        model.addAttribute("domainCode", domainCode);
        model.addAttribute("formAction", "/admin/users");
        model.addAttribute("submitLabel", "Save User");
        populateRoleModel(model, selectedRoleCode);
    }

    private void populateEditFormModel(Model model, Long userId, String domainCode, String selectedRoleCode) {
        model.addAttribute("pageTitle", "Editar usuario admin");
        model.addAttribute("isEdit", true);
        model.addAttribute("userId", userId);
        model.addAttribute("domainCode", domainCode);
        model.addAttribute("formAction", "/admin/users/" + userId);
        model.addAttribute("submitLabel", "Save Changes");
        populateRoleModel(model, selectedRoleCode);
    }

    private void populateRoleModel(Model model, String selectedRoleCode) {
        List<RoleOption> roles = adminUserService.listRoles()
            .stream()
            .map(ability -> new RoleOption(
                ability.getCode(),
                ability.getName(),
                ability.getPermissions().stream()
                    .sorted(Comparator.comparing(Permission::getCode))
                    .map(permission -> permission.getResource() + ":" + permission.getAction())
                    .toList()
            ))
            .toList();

        model.addAttribute("roles", roles);
        model.addAttribute("selectedRoleCode", selectedRoleCode);
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

    public record RoleOption(String code, String name, List<String> permissions) {
    }
}
