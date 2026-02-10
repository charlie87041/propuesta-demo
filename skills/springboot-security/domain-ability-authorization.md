# Domain-Ability-Permission Authorization Pattern

## Overview

This pattern implements a hierarchical authorization model with three levels:

```
Domain → Ability → Permission
```

- **Domain**: Bounded context, tenant, or organizational unit (e.g., `main-cookie-store`, `franchise-nyc`)
- **Ability**: Logical grouping of related permissions (e.g., `ManageInventory`, `ProcessOrders`)
- **Permission**: Atomic action that can be allowed or denied (e.g., `list-products`, `update-stock`)

## Why This Pattern?

| Approach | Limitation | Domain-Ability Solves |
|----------|------------|----------------------|
| Simple RBAC | Roles are flat, permissions are global | Contextual permissions per domain |
| ACL | Per-resource, doesn't scale | Logical grouping via abilities |
| ABAC | Complex policy engines | Simpler hierarchy, still flexible |

## Data Model

### Entities

```java
@Entity
@Table(name = "domains")
public class Domain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String code;  // e.g., "main-cookie-store"
    
    @Column(nullable = false)
    private String name;  // e.g., "Main Cookie Store"
    
    private String description;
    
    @Column(nullable = false)
    private boolean active = true;
}

@Entity
@Table(name = "abilities")
public class Ability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String code;  // e.g., "manage-inventory"
    
    @Column(nullable = false)
    private String name;  // e.g., "Manage Inventory"
    
    private String description;
    
    @ManyToMany
    @JoinTable(
        name = "ability_permissions",
        joinColumns = @JoinColumn(name = "ability_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
}

@Entity
@Table(name = "permissions")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String code;  // e.g., "products:list"
    
    @Column(nullable = false)
    private String name;  // e.g., "List Products"
    
    private String description;
    
    @Column(nullable = false)
    private String resource;  // e.g., "products"
    
    @Column(nullable = false)
    private String action;  // e.g., "list", "create", "update", "delete"
}

@Entity
@Table(name = "user_domain_abilities")
public class UserDomainAbility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "domain_id")
    private Domain domain;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "ability_id")
    private Ability ability;
    
    @Column(nullable = false)
    private boolean granted = true;  // true = allow, false = deny
    
    // Unique constraint: user + domain + ability
}

// For fine-grained overrides at permission level
@Entity
@Table(name = "user_domain_permission_overrides")
public class UserDomainPermissionOverride {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(optional = false)
    private User user;
    
    @ManyToOne(optional = false)
    private Domain domain;
    
    @ManyToOne(optional = false)
    private Permission permission;
    
    @Column(nullable = false)
    private boolean granted;  // Override: explicitly allow or deny
}
```

### ER Diagram

```
┌──────────┐       ┌──────────────────────┐       ┌──────────┐
│  Domain  │       │  UserDomainAbility   │       │   User   │
├──────────┤       ├──────────────────────┤       ├──────────┤
│ id       │◄──────│ domain_id            │       │ id       │
│ code     │       │ user_id              │──────►│ email    │
│ name     │       │ ability_id           │       │ ...      │
└──────────┘       │ granted              │       └──────────┘
                   └──────────────────────┘
                              │
                              ▼
┌──────────┐       ┌──────────────────────┐       ┌────────────┐
│ Ability  │◄──────│   ability_id         │       │ Permission │
├──────────┤       └──────────────────────┘       ├────────────┤
│ id       │                                      │ id         │
│ code     │◄─────────────────────────────────────│ code       │
│ name     │         ability_permissions          │ resource   │
└──────────┘              (M:N)                   │ action     │
                                                  └────────────┘
```

## Authorization Service

```java
@Service
public class DomainAuthorizationService {
    
    private final UserDomainAbilityRepository userDomainAbilityRepo;
    private final UserDomainPermissionOverrideRepository overrideRepo;
    
    /**
     * Check if user has permission in domain.
     * 
     * Resolution order:
     * 1. Check explicit permission overrides (deny takes precedence)
     * 2. Check abilities granted in domain
     * 3. Default: deny
     */
    public boolean hasPermission(Long userId, String domainCode, String permissionCode) {
        // 1. Check explicit overrides first
        Optional<UserDomainPermissionOverride> override = 
            overrideRepo.findByUserIdAndDomainCodeAndPermissionCode(
                userId, domainCode, permissionCode);
        
        if (override.isPresent()) {
            return override.get().isGranted();
        }
        
        // 2. Check if any granted ability includes this permission
        return userDomainAbilityRepo.existsGrantedAbilityWithPermission(
            userId, domainCode, permissionCode);
    }
    
    /**
     * Check if user has any ability in domain.
     */
    public boolean hasAbility(Long userId, String domainCode, String abilityCode) {
        return userDomainAbilityRepo.existsByUserIdAndDomainCodeAndAbilityCodeAndGrantedTrue(
            userId, domainCode, abilityCode);
    }
    
    /**
     * Check if user has access to domain at all.
     */
    public boolean hasDomainAccess(Long userId, String domainCode) {
        return userDomainAbilityRepo.existsByUserIdAndDomainCodeAndGrantedTrue(
            userId, domainCode);
    }
    
    /**
     * Get all permissions for user in domain.
     */
    public Set<String> getPermissions(Long userId, String domainCode) {
        Set<String> permissions = new HashSet<>();
        
        // Get all permissions from granted abilities
        permissions.addAll(
            userDomainAbilityRepo.findAllPermissionCodesByUserAndDomain(userId, domainCode));
        
        // Apply overrides
        List<UserDomainPermissionOverride> overrides = 
            overrideRepo.findAllByUserIdAndDomainCode(userId, domainCode);
        
        for (var override : overrides) {
            if (override.isGranted()) {
                permissions.add(override.getPermission().getCode());
            } else {
                permissions.remove(override.getPermission().getCode());
            }
        }
        
        return permissions;
    }
}
```

## Spring Security Integration

### Custom Annotation

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("@domainAuthz.check(#domainCode, '{permission}')")
public @interface RequiresPermission {
    String value();  // Permission code, e.g., "products:list"
    String domain() default "";  // If empty, extracted from path/request
}

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("@domainAuthz.checkAbility(#domainCode, '{ability}')")
public @interface RequiresAbility {
    String value();  // Ability code, e.g., "manage-inventory"
    String domain() default "";
}
```

### Authorization Component

```java
@Component("domainAuthz")
public class DomainAuthorizationEvaluator {
    
    private final DomainAuthorizationService authzService;
    
    /**
     * SpEL-callable method for @PreAuthorize.
     */
    public boolean check(String domainCode, String permissionCode) {
        Long userId = getCurrentUserId();
        return authzService.hasPermission(userId, domainCode, permissionCode);
    }
    
    public boolean checkAbility(String domainCode, String abilityCode) {
        Long userId = getCurrentUserId();
        return authzService.hasAbility(userId, domainCode, abilityCode);
    }
    
    public boolean checkDomainAccess(String domainCode) {
        Long userId = getCurrentUserId();
        return authzService.hasDomainAccess(userId, domainCode);
    }
    
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getId();
        }
        throw new AccessDeniedException("User not authenticated");
    }
}
```

### Controller Usage

```java
@RestController
@RequestMapping("/api/domains/{domainCode}/products")
public class ProductController {
    
    @GetMapping
    @RequiresPermission("products:list")
    public ResponseEntity<Page<ProductDTO>> listProducts(
            @PathVariable String domainCode,
            Pageable pageable) {
        // domainCode automatically used for permission check
        return ResponseEntity.ok(productService.findAll(domainCode, pageable));
    }
    
    @PostMapping
    @RequiresPermission("products:create")
    public ResponseEntity<ProductDTO> createProduct(
            @PathVariable String domainCode,
            @RequestBody @Valid CreateProductRequest request) {
        return ResponseEntity.ok(productService.create(domainCode, request));
    }
    
    @PutMapping("/{productId}/stock")
    @RequiresPermission("inventory:update-stock")
    public ResponseEntity<ProductDTO> updateStock(
            @PathVariable String domainCode,
            @PathVariable Long productId,
            @RequestBody UpdateStockRequest request) {
        return ResponseEntity.ok(productService.updateStock(domainCode, productId, request));
    }
}
```

## Standard Abilities and Permissions

### Suggested Structure

```yaml
# abilities.yml - Seed data for standard abilities

abilities:
  - code: manage-inventory
    name: Manage Inventory
    description: Full control over product inventory
    permissions:
      - products:list
      - products:read
      - products:create
      - products:update
      - products:delete
      - inventory:update-stock
      - inventory:view-alerts
      
  - code: view-catalog
    name: View Catalog
    description: Read-only access to product catalog
    permissions:
      - products:list
      - products:read
      - categories:list
      - categories:read
      
  - code: process-orders
    name: Process Orders
    description: Handle order fulfillment workflow
    permissions:
      - orders:list
      - orders:read
      - orders:update-status
      - orders:add-tracking
      
  - code: manage-orders
    name: Manage Orders
    description: Full control over orders including cancellation
    permissions:
      - orders:list
      - orders:read
      - orders:update-status
      - orders:add-tracking
      - orders:cancel
      - orders:refund
      
  - code: manage-customers
    name: Manage Customers
    description: Customer account management
    permissions:
      - customers:list
      - customers:read
      - customers:update
      - customers:disable
      
  - code: view-reports
    name: View Reports
    description: Access to business reports and analytics
    permissions:
      - reports:sales
      - reports:inventory
      - reports:customers
      
  - code: admin
    name: Administrator
    description: Full system access
    permissions:
      - "*"  # Wildcard for all permissions
```

### Permission Naming Convention

```
{resource}:{action}

Resources: products, categories, orders, customers, inventory, reports, settings
Actions: list, read, create, update, delete, export, import

Examples:
- products:list
- products:create
- orders:update-status
- inventory:update-stock
- reports:export
```

## Testing Pattern

### Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class DomainAuthorizationServiceTest {
    
    @Mock
    private UserDomainAbilityRepository abilityRepo;
    
    @Mock
    private UserDomainPermissionOverrideRepository overrideRepo;
    
    @InjectMocks
    private DomainAuthorizationService authzService;
    
    @Test
    void hasPermission_whenGrantedViaAbility_shouldReturnTrue() {
        // Given
        when(overrideRepo.findByUserIdAndDomainCodeAndPermissionCode(1L, "store-1", "products:list"))
            .thenReturn(Optional.empty());
        when(abilityRepo.existsGrantedAbilityWithPermission(1L, "store-1", "products:list"))
            .thenReturn(true);
        
        // When
        boolean result = authzService.hasPermission(1L, "store-1", "products:list");
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    void hasPermission_whenExplicitlyDenied_shouldReturnFalse() {
        // Given - permission is in ability but explicitly denied
        var override = new UserDomainPermissionOverride();
        override.setGranted(false);
        when(overrideRepo.findByUserIdAndDomainCodeAndPermissionCode(1L, "store-1", "products:delete"))
            .thenReturn(Optional.of(override));
        
        // When
        boolean result = authzService.hasPermission(1L, "store-1", "products:delete");
        
        // Then
        assertThat(result).isFalse();
        verify(abilityRepo, never()).existsGrantedAbilityWithPermission(any(), any(), any());
    }
    
    @Test
    void hasPermission_whenNoAbilityGranted_shouldReturnFalse() {
        // Given
        when(overrideRepo.findByUserIdAndDomainCodeAndPermissionCode(1L, "store-1", "products:list"))
            .thenReturn(Optional.empty());
        when(abilityRepo.existsGrantedAbilityWithPermission(1L, "store-1", "products:list"))
            .thenReturn(false);
        
        // When
        boolean result = authzService.hasPermission(1L, "store-1", "products:list");
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    void hasPermission_whenDifferentDomain_shouldReturnFalse() {
        // Given - user has permission in store-1 but not store-2
        when(overrideRepo.findByUserIdAndDomainCodeAndPermissionCode(1L, "store-2", "products:list"))
            .thenReturn(Optional.empty());
        when(abilityRepo.existsGrantedAbilityWithPermission(1L, "store-2", "products:list"))
            .thenReturn(false);
        
        // When
        boolean result = authzService.hasPermission(1L, "store-2", "products:list");
        
        // Then
        assertThat(result).isFalse();
    }
}
```

### Integration Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerAuthorizationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(username = "user-with-permission")
    void listProducts_whenHasPermission_shouldReturn200() throws Exception {
        // Given: user has products:list in domain "store-1"
        
        mockMvc.perform(get("/api/domains/store-1/products"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "user-without-permission")
    void listProducts_whenNoPermission_shouldReturn403() throws Exception {
        // Given: user does NOT have products:list in domain "store-1"
        
        mockMvc.perform(get("/api/domains/store-1/products"))
            .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "user-wrong-domain")
    void listProducts_whenPermissionInDifferentDomain_shouldReturn403() throws Exception {
        // Given: user has products:list in "store-1" but accessing "store-2"
        
        mockMvc.perform(get("/api/domains/store-2/products"))
            .andExpect(status().isForbidden());
    }
    
    @Test
    void listProducts_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/domains/store-1/products"))
            .andExpect(status().isUnauthorized());
    }
}
```

## Migration Guide

### From Simple RBAC

```java
// Before: Simple role check
@PreAuthorize("hasRole('ADMIN')")
public void updateStock(...) { }

// After: Domain-ability-permission check
@RequiresPermission("inventory:update-stock")
public void updateStock(@PathVariable String domainCode, ...) { }
```

### Adding Domain to URLs

```
Before: /api/products
After:  /api/domains/{domainCode}/products

Before: /api/orders/{orderId}
After:  /api/domains/{domainCode}/orders/{orderId}
```

## Best Practices

1. **Deny by Default**: If no ability grants the permission, access is denied
2. **Explicit Denials Win**: Override denials take precedence over ability grants
3. **Domain in URL**: Include domain code in API paths for clear context
4. **Audit Everything**: Log all permission checks for security auditing
5. **Cache Permissions**: User permissions rarely change; cache aggressively
6. **Seed Standard Abilities**: Provide sensible defaults, allow customization
7. **Test All Paths**: Unit test the service, integration test the endpoints
