# Authorization Model (common.authorization)

Este módulo implementa autorización por dominio con el patrón:

- `Domain` -> contexto lógico/tenant (ej. `main-store`)
- `Ability` -> paquete de permisos (ej. `manage-inventory`)
- `Permission` -> acción atómica (ej. `products:create`)

## Componentes principales

- Entidades:
  - `Domain`
  - `Ability` (M:N con `Permission`)
  - `Permission`
  - `UserDomainAbility` (grant/revoke por usuario+dominio+ability)
  - `UserDomainPermissionOverride` (override allow/deny por usuario+dominio+permission)
- Servicio:
  - `DomainAuthorizationService`
- Evaluación en runtime:
  - `DomainAuthorizationEvaluator`
  - `AuthorizationAspect`
- Anotaciones:
  - `@RequiresPermission("resource:action")`
  - `@RequiresAbility("ability-code")`
- Seed inicial:
  - `AuthorizationDataSeeder`

## Regla de resolución de permisos

En `DomainAuthorizationService`, el permiso efectivo se calcula así:

1. Se agregan permisos desde `UserDomainAbility` con `granted=true`.
2. Se aplican overrides (`UserDomainPermissionOverride`) en el mismo dominio:
  - `granted=true` => agrega permiso.
  - `granted=false` => elimina permiso.
3. Resultado final:
  - `hasPermission(...)` es `true` si está el permiso exacto o `*`.
  - Si no existe grant/override aplicable, default es deny.

Resumen: **override > ability > deny**.

## Cómo usarlo en controllers

Importante: el endpoint debe incluir `domainCode` en path para mantener aislamiento por dominio.

```java
@RestController
@RequestMapping("/api/domains/{domainCode}/admin/products")
class AdminProductController {

    @PostMapping
    @RequiresPermission("products:create")
    public ProductDto create(@PathVariable String domainCode, @RequestBody CreateProductRequest req) {
        // ...
    }

    @PutMapping("/{id}/stock")
    @RequiresPermission("inventory:update-stock")
    public ProductDto updateStock(@PathVariable String domainCode, @PathVariable Long id, @RequestBody UpdateStockRequest req) {
        // ...
    }

    @GetMapping("/reports")
    @RequiresAbility("view-reports")
    public ReportDto reports(@PathVariable String domainCode) {
        // ...
    }
}
```

## Requisitos de autenticación

- `SecurityConfig` exige autenticación para endpoints no públicos.
- `JwtAuthenticationFilter` coloca el `userId` como principal cuando el token es válido.
- Si no hay autenticación:
  - Spring Security responde `401`.
- Si hay autenticación pero sin permiso/ability:
  - `AuthorizationAspect` dispara `AccessDeniedException` -> `403`.

## Seed de autorización

`AuthorizationDataSeeder` crea datos base idempotentes:

- Dominio: `main-store`
- Abilities: 13
- Permissions: 40+
- Mappings ability -> permission

Esto permite arrancar con un baseline consistente para pruebas e integración.

## Consultas de servicio útiles

`DomainAuthorizationService` expone:

- `hasPermission(userId, domainCode, permissionCode)`
- `hasAbility(userId, domainCode, abilityCode)`
- `hasDomainAccess(userId, domainCode)`
- `getPermissions(userId, domainCode)`

Recomendado usar estas APIs en servicios de dominio cuando necesites chequeos explícitos fuera del controller.

## Buenas prácticas

- Usar permisos granulares en endpoints mutativos (`create/update/delete/...`).
- Usar abilities para reglas de alto nivel (pantallas/áreas funcionales completas).
- No reutilizar `domainCode` hardcodeado; tomarlo siempre del path/request actual.
- Mantener nuevos permisos y mappings en el seeder para evitar drift entre entornos.
