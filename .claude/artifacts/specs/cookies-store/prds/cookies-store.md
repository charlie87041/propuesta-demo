# Cookies Store E-Commerce Platform PRD

## Overview

Plataforma e-commerce completa para venta de galletas artesanales. Implementación usando TDD con enfoque Red-Green-Refactor.

**Tecnologías**: Spring Boot 3.2.x, Java 21+, Gradle 8.x, PostgreSQL 15+, Redis 7+, jqwik, JUnit 5, Mockito, TestContainers, React 18, TypeScript

**Principios TDD**:
1. **Red Phase**: Escribir tests que fallen primero
2. **Green Phase**: Implementar código mínimo para pasar tests
3. **Refactor Phase**: Mejorar el código manteniendo tests verdes

---

## Milestones

### Milestone 1: Project Setup & Infrastructure
**Duration**: 2-3 days
**Goal**: Establecer la base del proyecto con estructura multi-módulo y configuración de testing infrastructure.

#### Issues

##### Issue 1.1: Initialize Gradle Multi-Module Project
**Labels**: enhancement, backend, infrastructure
**Estimate**: 0.5 days

**Description**:
Configurar la estructura base del proyecto con Gradle multi-módulo.

**TDD Approach**:
- Red Phase: Verificar que build.gradle raíz existe, verificar que settings.gradle incluye todos los módulos
- Green Phase: Crear estructura con módulos: `common/`, `catalog-module/`, `cart-module/`, `customer-module/`, `order-module/`, `payment-module/`, `admin-module/`, `application/`
- Refactor Phase: Extraer versiones a gradle.properties, configurar plugins comunes

**Acceptance Criteria**:
- [ ] Estructura multi-módulo creada
- [ ] `./gradlew build` ejecuta exitosamente
- [ ] Todos los módulos compilan

---

##### Issue 1.2: Configure PostgreSQL with TestContainers
**Labels**: enhancement, backend, testing, infrastructure
**Estimate**: 0.5 days

**Description**:
Configurar TestContainers para tests de integración con PostgreSQL.

**TDD Approach**:
- Red Phase: Test base que verifica conexión a BD, test que verifica schema se crea
- Green Phase: Crear `AbstractIntegrationTest`, configurar PostgreSQL container, configurar `application-test.yml`
- Refactor Phase: Extraer configuración reutilizable

**Acceptance Criteria**:
- [ ] TestContainers configurado para PostgreSQL
- [ ] Tests de integración usan contenedor efímero
- [ ] Schema se crea automáticamente

---

##### Issue 1.3: Configure Redis with TestContainers
**Labels**: enhancement, backend, testing, infrastructure
**Estimate**: 0.5 days

**Description**:
Configurar TestContainers para tests de integración con Redis.

**TDD Approach**:
- Red Phase: Test que verifica conexión a Redis, test básico de cache
- Green Phase: Configurar Redis container en base test, configurar `RedisTemplate`
- Refactor Phase: Abstraer configuración

**Acceptance Criteria**:
- [ ] Redis container para tests
- [ ] Cache funciona en tests
- [ ] Configuración reutilizable

---

### Milestone 2: Common Modules
**Duration**: 4-5 days
**Goal**: Implementar módulos comunes: autenticación JWT, seguridad, respuestas API y sistema de autorización Domain-Ability-Permission.

#### Issues

##### Issue 2.1: Implement JWT Token Provider (auth-common)
**Labels**: enhancement, backend, security
**Estimate**: 0.5 days

**Description**:
Generación y validación de tokens JWT.

**TDD Approach**:
- Red Phase: Test generación token con userId válido, test extracción userId, test validación token expirado, Property Test round-trip userId
- Green Phase: Crear `JwtTokenProvider`, implementar con jjwt
- Refactor Phase: Extraer configuración a properties

**Acceptance Criteria**:
- [ ] Tokens JWT generados correctamente
- [ ] Validación detecta tokens expirados/inválidos
- [ ] Configuración externalizada

---

##### Issue 2.2: Implement Security Filter Chain (security-common)
**Labels**: enhancement, backend, security
**Estimate**: 0.5 days

**Description**:
Configuración de Spring Security con JWT.

**TDD Approach**:
- Red Phase: Test endpoints públicos accesibles sin auth, test endpoints protegidos requieren token, test token inválido retorna 401
- Green Phase: Crear `SecurityConfig`, crear `JwtAuthenticationFilter`, configurar rutas públicas vs protegidas
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Endpoints públicos accesibles
- [ ] Endpoints protegidos requieren JWT válido
- [ ] Respuestas 401/403 apropiadas

---

##### Issue 2.3: Implement API Response Wrapper (common-utils)
**Labels**: enhancement, backend
**Estimate**: 0.5 days

**Description**:
Formato estándar de respuestas API.

**TDD Approach**:
- Red Phase: Test success response format, test error response format, test validation error format
- Green Phase: Crear `ApiResponse<T>` record, crear `ApiError` record, crear `GlobalExceptionHandler`
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Todas las respuestas siguen formato estándar
- [ ] Errores incluyen código y mensaje
- [ ] Errores de validación incluyen detalles por campo

---

##### Issue 2.4: Implement Domain-Ability-Permission Entities (authorization-common)
**Labels**: enhancement, backend, security, authorization
**Estimate**: 1 day

**Description**:
Entidades para el modelo de autorización jerárquico Domain-Ability-Permission.

**TDD Approach**:
- Red Phase: Test crear Domain persiste correctamente, test crear Ability con permissions, test crear Permission con resource:action, test UserDomainAbility grant/revoke, test UserDomainPermissionOverride
- Green Phase: Crear entidades `Domain`, `Ability`, `Permission`, `UserDomainAbility`, `UserDomainPermissionOverride`, crear repositories
- Refactor Phase: Agregar índices para lookups rápidos, documentar relaciones

**Acceptance Criteria**:
- [ ] Todas las entidades persisten correctamente
- [ ] Relaciones M:N funcionan (Ability ↔ Permission)
- [ ] Constraints únicos funcionan (user + domain + ability)
- [ ] Índices creados para performance

---

##### Issue 2.5: Implement DomainAuthorizationService (authorization-common)
**Labels**: enhancement, backend, security, authorization
**Estimate**: 1.5 days

**Description**:
Servicio de autorización con lógica de resolución de permisos.

**TDD Approach**:
- Red Phase: Test `hasPermission` retorna true cuando ability lo incluye, test retorna false cuando no hay ability, test override deny toma precedencia, test override allow agrega permiso, test `hasAbility`, test `hasDomainAccess`, test `getPermissions`
- Green Phase: Crear `DomainAuthorizationService`, implementar resolución: override → ability → deny
- Refactor Phase: Agregar caching de permisos por usuario/dominio

**Acceptance Criteria**:
- [ ] Resolución de permisos correcta
- [ ] Overrides funcionan (deny y allow)
- [ ] Domain isolation funciona
- [ ] Performance < 10ms para hasPermission

---

##### Issue 2.6: Implement Authorization Annotations (authorization-common)
**Labels**: enhancement, backend, security, authorization
**Estimate**: 1 day

**Description**:
Anotaciones `@RequiresPermission` y `@RequiresAbility` con evaluador SpEL.

**TDD Approach**:
- Red Phase: Test `@RequiresPermission` permite acceso con permiso, test retorna 403 sin permiso, test retorna 403 en domain incorrecto, test `@RequiresAbility`, test usuario no autenticado retorna 401
- Green Phase: Crear anotaciones, crear `DomainAuthorizationEvaluator`, integrar con `@PreAuthorize`
- Refactor Phase: Agregar soporte para domainCode desde path variable

**Acceptance Criteria**:
- [ ] Anotaciones funcionan en controllers
- [ ] Domain extraído automáticamente de path
- [ ] 401 para no autenticados, 403 para sin permiso
- [ ] SpEL expressions funcionan

---

##### Issue 2.7: Seed Authorization Data
**Labels**: enhancement, backend, security, authorization
**Estimate**: 0.5 days

**Description**:
Datos iniciales de domains, abilities y permissions.

**TDD Approach**:
- Red Phase: Test domain "main-store" existe después de seed, test todas las abilities existen, test todas las permissions existen, test mappings correctos
- Green Phase: Crear `DataSeeder` o Flyway migration, insertar datos
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Domain "main-store" creado
- [ ] 13 abilities creadas
- [ ] 40+ permissions creadas
- [ ] Mappings correctos

---

### Milestone 3: Catalog Module
**Duration**: 3-4 days
**Goal**: Implementar catálogo de productos con categorías, búsqueda, paginación y caching.

#### Issues

##### Issue 3.1: Implement Category Entity and Repository
**Labels**: enhancement, backend
**Estimate**: 0.5 days

**Description**:
Entidad Category con operaciones CRUD.

**TDD Approach**:
- Red Phase: Test crear categoría persiste, test encontrar por slug, test listar ordenadas, Property Test slug único
- Green Phase: Crear entidad `Category`, crear `CategoryRepository`
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Categorías se persisten correctamente
- [ ] Búsqueda por slug funciona
- [ ] Slugs son únicos

---

##### Issue 3.2: Implement Product Entity and Repository
**Labels**: enhancement, backend
**Estimate**: 0.5 days

**Description**:
Entidad Product con relación a Category.

**TDD Approach**:
- Red Phase: Test crear producto con categoría, test encontrar por slug, test filtrar por categoría, test buscar por nombre, test paginación
- Green Phase: Crear entidad `Product`, crear `ProductRepository` con queries custom
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Productos se relacionan con categorías
- [ ] Búsqueda y filtrado funcionan
- [ ] Paginación implementada

---

##### Issue 3.3: Implement Catalog Service
**Labels**: enhancement, backend
**Estimate**: 0.5 days

**Description**:
Lógica de negocio para catálogo.

**TDD Approach**:
- Red Phase: Test listar productos paginados, test filtrar por categoría, test buscar por keyword, test obtener detalle, test productos relacionados
- Green Phase: Crear `ProductService`, `CategoryService`, implementar DTOs
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Servicios implementan toda la lógica
- [ ] DTOs exponen datos apropiados
- [ ] Sin lógica en controladores

---

##### Issue 3.4: Implement Catalog REST Controllers
**Labels**: enhancement, backend, api
**Estimate**: 0.5 days

**Description**:
Endpoints REST para catálogo.

**TDD Approach**:
- Red Phase: Test GET /api/products lista paginada, test filtro por categoryId, test búsqueda, test GET detalle, test GET categorías
- Green Phase: Crear `ProductController`, `CategoryController`
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Endpoints responden correctamente
- [ ] Query params funcionan
- [ ] Respuestas siguen formato estándar

---

##### Issue 3.5: Implement Catalog Caching
**Labels**: enhancement, backend, performance
**Estimate**: 0.5 days

**Description**:
Cache de Redis para catálogo.

**TDD Approach**:
- Red Phase: Test primera llamada va a BD, test segunda usa cache, test update invalida cache
- Green Phase: Agregar `@Cacheable` annotations, configurar cache manager
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Queries cacheados en Redis
- [ ] Cache se invalida en updates
- [ ] TTL configurado (5 min)

---

### Milestone 4: Customer Module
**Duration**: 3-4 days
**Goal**: Implementar registro, autenticación, perfil y gestión de direcciones.

#### Issues

##### Issue 4.1: Implement Customer Entity and Repository
**Labels**: enhancement, backend
**Estimate**: 0.5 days

**Description**:
Entidad Customer para usuarios registrados.

**TDD Approach**:
- Red Phase: Test crear customer con email único, test encontrar por email, test password se hashea
- Green Phase: Crear entidad `Customer`, crear `CustomerRepository`
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Emails son únicos
- [ ] Passwords hasheados con BCrypt

---

##### Issue 4.2: Implement Address Entity and Repository
**Labels**: enhancement, backend
**Estimate**: 0.5 days

**Description**:
Entidad Address para direcciones de envío.

**TDD Approach**:
- Red Phase: Test crear address para customer, test listar addresses, test marcar como default
- Green Phase: Crear entidad `Address`, crear `AddressRepository`
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Múltiples addresses por customer
- [ ] Solo una default address

---

##### Issue 4.3: Implement Registration Service
**Labels**: enhancement, backend
**Estimate**: 0.5 days

**Description**:
Registro de nuevos usuarios.

**TDD Approach**:
- Red Phase: Test registro crea customer, test email duplicado falla, test verification token generado, test merge guest cart
- Green Phase: Crear `RegistrationService`, `EmailVerificationService`
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Registro exitoso
- [ ] Validación de email único
- [ ] Email verification funciona

---

##### Issue 4.4: Implement Authentication Service
**Labels**: enhancement, backend, security
**Estimate**: 0.5 days

**Description**:
Login y gestión de sesiones.

**TDD Approach**:
- Red Phase: Test login válido retorna token, test login inválido falla, test password reset flow
- Green Phase: Crear `AuthenticationService`, `PasswordResetService`
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Login genera JWT
- [ ] Credenciales inválidas = 401
- [ ] Password reset funciona

---

##### Issue 4.5: Implement Customer REST Controllers
**Labels**: enhancement, backend, api, security
**Estimate**: 1 day

**Description**:
Endpoints para auth y perfil con validación de ownership.

**TDD Approach**:
- Red Phase: Test POST register, test POST login, test GET /me, test CRUD addresses
- Authorization Tests: Test GET /me sin auth retorna 401, test addresses sin auth 401, test ownership validation
- Green Phase: Crear `AuthController`, `CustomerController`, agregar `@RequiresPermission`
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Registro y login funcionan
- [ ] Perfil y addresses CRUD completo
- [ ] 401 para endpoints protegidos sin auth
- [ ] Ownership validation para addresses

---

### Milestone 5: Cart Module
**Duration**: 2-3 days
**Goal**: Implementar carrito de compras con soporte para usuarios y guests.

#### Issues

##### Issue 5.1: Implement Cart Entity and Repository
**Labels**: enhancement, backend
**Estimate**: 0.5 days

**Description**:
Entidad Cart con items.

**TDD Approach**:
- Red Phase: Test crear cart para customer, test crear cart para guest, test agregar items
- Green Phase: Crear entidades `Cart`, `CartItem`, crear repositories
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Carts para users y guests
- [ ] Items relacionados a products

---

##### Issue 5.2: Implement Cart Service
**Labels**: enhancement, backend
**Estimate**: 0.5 days

**Description**:
Lógica de carrito de compras.

**TDD Approach**:
- Red Phase: Test agregar producto, test actualizar cantidad, test remover, test validar stock, test calcular totales, test merge guest cart
- Green Phase: Crear `CartService`, implementar stock validation
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] CRUD de items funciona
- [ ] Validación de stock
- [ ] Merge de guest cart

---

##### Issue 5.3: Implement Cart Caching
**Labels**: enhancement, backend, performance
**Estimate**: 0.5 days

**Description**:
Cache de carritos en Redis.

**TDD Approach**:
- Red Phase: Test cart se cachea, test modificación actualiza cache, test expiration 24h
- Green Phase: Implementar Redis cache, configurar TTL
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Carts cacheados
- [ ] Performance mejorado

---

##### Issue 5.4: Implement Cart REST Controller
**Labels**: enhancement, backend, api, security
**Estimate**: 1 day

**Description**:
Endpoints de carrito con ownership validation.

**TDD Approach**:
- Red Phase: Test GET /cart, test POST items, test PUT/DELETE items
- Authorization Tests: Test guest con sessionId, test customer con su cart, test acceso a cart de otro 403
- Green Phase: Crear `CartController`, agregar `@RequiresPermission`, implementar ownership validation
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] CRUD endpoints funcionan
- [ ] Soporta auth y guest
- [ ] Ownership validation para cart items
- [ ] 403 al acceder cart de otro usuario

---

### Milestone 6: Order Module
**Duration**: 4-5 days
**Goal**: Implementar proceso de checkout, gestión de órdenes y notificaciones.

#### Issues

##### Issue 6.1: Implement Order Entity and Repository
**Labels**: enhancement, backend
**Estimate**: 0.5 days

**Description**:
Entidades Order y OrderItem.

**TDD Approach**:
- Red Phase: Test crear order desde cart, test order number único, test items inmutables
- Green Phase: Crear entidades `Order`, `OrderItem`, crear repositories
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Orders creados desde carts
- [ ] Order numbers únicos
- [ ] Items preservan precio al momento

---

##### Issue 6.2: Implement Checkout Service
**Labels**: enhancement, backend
**Estimate**: 0.5 days

**Description**:
Proceso de checkout.

**TDD Approach**:
- Red Phase: Test validar cart, test crear order reserva stock, test stock insuficiente falla, test calcular shipping
- Green Phase: Crear `CheckoutService`, implementar stock reservation
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Validación pre-checkout
- [ ] Reserva de stock atómica
- [ ] Cálculo de shipping

---

##### Issue 6.3: Implement Order Service
**Labels**: enhancement, backend
**Estimate**: 0.5 days

**Description**:
Gestión de órdenes.

**TDD Approach**:
- Red Phase: Test obtener órdenes de customer, test detalle, test cancelar (solo pending/paid), test transiciones válidas
- Green Phase: Crear `OrderService`, implementar state machine
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] CRUD de órdenes
- [ ] Estado machine válido
- [ ] Cancelación restaura stock

---

##### Issue 6.4: Implement Order Notifications
**Labels**: enhancement, backend
**Estimate**: 0.5 days

**Description**:
Notificaciones de cambio de estado.

**TDD Approach**:
- Red Phase: Test estado change envía email, test email contiene info correcta
- Green Phase: Crear `OrderNotificationService`, integrar SendGrid
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Emails enviados en cambios de estado
- [ ] Templates correctos

---

##### Issue 6.5: Implement Order REST Controllers
**Labels**: enhancement, backend, api, security
**Estimate**: 1.5 days

**Description**:
Endpoints de checkout y órdenes con ownership validation.

**TDD Approach**:
- Red Phase: Test POST checkout, test GET orders, test GET order detail, test POST cancel
- Authorization Tests: Test checkout sin auth 401, test orders solo propias, test order de otro 403, test cancel de otro 403, test permiso `orders:cancel-own`
- Green Phase: Crear `CheckoutController`, `OrderController`, agregar `@RequiresPermission`
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Checkout flow completo
- [ ] Order management endpoints
- [ ] 401 para endpoints sin auth
- [ ] 403 para acceso a órdenes de otros
- [ ] Ownership validation funciona

---

### Milestone 7: Payment Module
**Duration**: 3-4 days
**Goal**: Implementar integración con Stripe para procesamiento de pagos.

#### Issues

##### Issue 7.1: Implement Payment Entity and Repository
**Labels**: enhancement, backend
**Estimate**: 0.5 days

**Description**:
Entidad Payment para transacciones.

**TDD Approach**:
- Red Phase: Test crear payment para order, test actualizar status
- Green Phase: Crear entidad `Payment`, crear `PaymentRepository`
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Payments relacionados a orders
- [ ] Estados de pago trackeables

---

##### Issue 7.2: Implement Stripe Integration
**Labels**: enhancement, backend, integration
**Estimate**: 1 day

**Description**:
Integración con Stripe para pagos.

**TDD Approach**:
- Red Phase: Test crear payment intent, test confirmar pago exitoso, test manejar pago fallido, test webhook signature
- Green Phase: Crear `StripePaymentGateway`, implementar webhook handler
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Payment intents creados
- [ ] Webhooks procesados
- [ ] Tokenización (no raw cards)

---

##### Issue 7.3: Implement Payment Service
**Labels**: enhancement, backend
**Estimate**: 0.5 days

**Description**:
Orquestación de pagos.

**TDD Approach**:
- Red Phase: Test iniciar pago crea intent, test pago exitoso actualiza order, test refund procesa
- Green Phase: Crear `PaymentService`, orquestar con OrderService
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Flow de pago completo
- [ ] Refunds funcionan
- [ ] Order status sincronizado

---

##### Issue 7.4: Implement Payment REST Controller
**Labels**: enhancement, backend, api
**Estimate**: 0.5 days

**Description**:
Endpoints de pago.

**TDD Approach**:
- Red Phase: Test POST /payments/intent, test POST /payments/confirm, test POST /payments/webhook
- Green Phase: Crear `PaymentController`
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Payment flow vía API
- [ ] Webhook endpoint seguro

---

### Milestone 8: Admin Module
**Duration**: 3-4 days
**Goal**: Implementar funcionalidades administrativas con control de acceso por Domain-Ability-Permission.

#### Issues

##### Issue 8.1: Implement Admin Product Management
**Labels**: enhancement, backend, admin, security
**Estimate**: 1.5 days

**Description**:
CRUD de productos para admins con Domain-Ability-Permission.

**TDD Approach**:
- Red Phase: Test crear producto (admin only), test actualizar, test stock, test toggle active
- Authorization Tests: Test sin auth 401, test customer sin ability 403, test `manage-inventory` puede crear, test `view-inventory` no puede crear, test domain diferente 403, test permissions específicos
- Green Phase: Crear `AdminProductService`, `AdminProductController` con path `/api/domains/{domainCode}/admin/products`, agregar `@RequiresPermission`
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Solo usuarios con permisos apropiados acceden
- [ ] CRUD completo de productos
- [ ] Domain isolation funciona
- [ ] 401/403 responses correctos

---

##### Issue 8.2: Implement Admin Order Management
**Labels**: enhancement, backend, admin, security
**Estimate**: 1.5 days

**Description**:
Gestión de órdenes para admins con Domain-Ability-Permission.

**TDD Approach**:
- Red Phase: Test listar órdenes pendientes, test actualizar estado, test tracking number
- Authorization Tests: Test sin auth 401, test sin ability 403, test `process-orders` ve y actualiza, test `process-orders` no puede refund, test `manage-orders` puede cancelar/refund, test domain isolation
- Green Phase: Crear `AdminOrderService`, `AdminOrderController` con path `/api/domains/{domainCode}/admin/orders`, agregar `@RequiresPermission`
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Fulfillment workflow completo
- [ ] Tracking integrado
- [ ] Permission-based access control
- [ ] Domain isolation funciona
- [ ] 401/403 responses correctos

---

##### Issue 8.3: Implement Low Stock Alerts
**Labels**: enhancement, backend, admin
**Estimate**: 0.5 days

**Description**:
Alertas cuando stock bajo umbral.

**TDD Approach**:
- Red Phase: Test stock bajo threshold genera alerta, test alerta enviada a admin
- Green Phase: Crear `InventoryAlertService`, implementar notificaciones
- Refactor Phase: N/A

**Acceptance Criteria**:
- [ ] Alertas automáticas
- [ ] Configuración de thresholds

---

### Milestone 9: Frontend (React)
**Duration**: 5-7 days
**Goal**: Implementar interfaz de usuario con React 18, TypeScript y TailwindCSS.

#### Issues

##### Issue 9.1: Setup React Project
**Labels**: enhancement, frontend
**Estimate**: 0.5 days

**Description**:
Inicializar frontend con React + TypeScript.

**Acceptance Criteria**:
- [ ] Vite + React 18 + TypeScript
- [ ] TailwindCSS configurado
- [ ] React Router configurado

---

##### Issue 9.2: Implement Product Catalog UI
**Labels**: enhancement, frontend
**Estimate**: 1.5 days

**Description**:
Páginas de catálogo y detalle.

**Acceptance Criteria**:
- [ ] Grid de productos con filtros
- [ ] Página de detalle de producto
- [ ] Búsqueda funcional

---

##### Issue 9.3: Implement Cart UI
**Labels**: enhancement, frontend
**Estimate**: 1 day

**Description**:
Carrito de compras interactivo.

**Acceptance Criteria**:
- [ ] Agregar/remover items
- [ ] Actualizar cantidades
- [ ] Resumen de totales

---

##### Issue 9.4: Implement Checkout Flow UI
**Labels**: enhancement, frontend
**Estimate**: 1.5 days

**Description**:
Proceso de checkout completo.

**Acceptance Criteria**:
- [ ] Selección de dirección
- [ ] Integración Stripe Elements
- [ ] Confirmación de orden

---

##### Issue 9.5: Implement Customer Account UI
**Labels**: enhancement, frontend
**Estimate**: 1.5 days

**Description**:
Páginas de cuenta de usuario.

**Acceptance Criteria**:
- [ ] Login/Register
- [ ] Perfil y direcciones
- [ ] Historial de órdenes

---

### Milestone 10: Security & Performance
**Duration**: 2-3 days
**Goal**: Implementar medidas de seguridad adicionales y optimizaciones de performance.

#### Issues

##### Issue 10.1: Implement Rate Limiting
**Labels**: enhancement, backend, security
**Estimate**: 0.5 days

**Description**:
Rate limiting en endpoints sensibles.

**Acceptance Criteria**:
- [ ] 10 req/min en auth endpoints
- [ ] 429 response cuando excede

---

##### Issue 10.2: Implement Audit Logging
**Labels**: enhancement, backend, security
**Estimate**: 0.5 days

**Description**:
Logging de acciones administrativas.

**Acceptance Criteria**:
- [ ] Todas las acciones admin loggeadas
- [ ] Logs incluyen user, action, timestamp

---

##### Issue 10.3: Performance Optimization
**Labels**: enhancement, backend, performance
**Estimate**: 1 day

**Description**:
Optimizaciones de performance.

**Acceptance Criteria**:
- [ ] Queries optimizados con índices
- [ ] Connection pooling configurado
- [ ] Catalog queries < 200ms p95

---

## Summary

| Milestone | Issues | Focus | Duration |
|-----------|--------|-------|----------|
| 1 | 1.1-1.3 | Infrastructure | 2-3 days |
| 2 | 2.1-2.7 | Common Modules + Authorization | 4-5 days |
| 3 | 3.1-3.5 | Catalog | 3-4 days |
| 4 | 4.1-4.5 | Customer | 3-4 days |
| 5 | 5.1-5.4 | Cart | 2-3 days |
| 6 | 6.1-6.5 | Order | 4-5 days |
| 7 | 7.1-7.4 | Payment | 3-4 days |
| 8 | 8.1-8.3 | Admin | 3-4 days |
| 9 | 9.1-9.5 | Frontend | 5-7 days |
| 10 | 10.1-10.3 | Security & Perf | 2-3 days |

**Total Issues**: 42
**Estimated Duration**: 7-9 weeks for MVP

## References

- [requirements.md](../artifacts/specs/cookies-store/requirements.md) - Requirements specification
- [design.md](../artifacts/specs/cookies-store/design.md) - Architecture design
- [authorization.md](../artifacts/specs/cookies-store/authorization.md) - Domain-Ability-Permission model
- [tasks.md](../artifacts/specs/cookies-store/tasks.md) - Original TDD task breakdown
