# Implementation Plan: Cookies Store E-Commerce Platform

## Overview

Este plan de implementación sigue un enfoque **Test-Driven Development (TDD)** estricto, donde cada tarea sigue el ciclo Red-Green-Refactor. Las tareas están organizadas en milestones lógicos que construyen el sistema incrementalmente.

**Tecnologías**: Spring Boot 3.2.x, Java 21+, Gradle 8.x, PostgreSQL 15+, Redis 7+, jqwik, JUnit 5, Mockito, TestContainers

**Principios TDD**:
1. **Red Phase**: Escribir tests que fallen primero
2. **Green Phase**: Implementar código mínimo para pasar tests
3. **Refactor Phase**: Mejorar el código manteniendo tests verdes

---

## Milestone 1: Project Setup & Infrastructure

### Task 1.1: Initialize Gradle Multi-Module Project

**Descripción**: Configurar la estructura base del proyecto con Gradle multi-módulo.

**Enfoque TDD**:
1. **Red Phase**:
   - Verificar que el build.gradle raíz existe
   - Verificar que settings.gradle incluye todos los módulos
   
2. **Green Phase**:
   - Crear estructura: `common/`, `catalog-module/`, `cart-module/`, `customer-module/`, `order-module/`, `payment-module/`, `admin-module/`, `application/`
   - Crear `build.gradle` raíz con Spring Boot 3.2.x, Java 21
   - Crear `settings.gradle` con todos los módulos

3. **Refactor Phase**:
   - Extraer versiones a gradle.properties
   - Configurar plugins comunes

**Acceptance Criteria**:
- [ ] Estructura multi-módulo creada
- [ ] `./gradlew build` ejecuta exitosamente
- [ ] Todos los módulos compilan

**Related Requirements**: R11, R12

---

### Task 1.2: Configure PostgreSQL with TestContainers

**Descripción**: Configurar TestContainers para tests de integración.

**Enfoque TDD**:
1. **Red Phase**:
   - Test de conexión a PostgreSQL container
   - Test de creación de esquema básico
   
2. **Green Phase**:
   - Agregar dependencias TestContainers
   - Crear `AbstractIntegrationTest` base class
   - Configurar `application-test.yml`

3. **Refactor Phase**:
   - Singleton container para performance

**Acceptance Criteria**:
- [ ] TestContainers inicia PostgreSQL
- [ ] Tests de integración conectan a BD
- [ ] Container cleanup automático

**Related Requirements**: R11, R12

---

### Task 1.3: Configure Redis with TestContainers

**Descripción**: Configurar Redis para caché de sesiones y carritos.

**Enfoque TDD**:
1. **Red Phase**:
   - Test de conexión a Redis container
   - Test de operaciones básicas get/set
   
2. **Green Phase**:
   - Agregar dependencias Redis
   - Configurar RedisTemplate
   - Agregar Redis a AbstractIntegrationTest

**Acceptance Criteria**:
- [ ] Redis container funcional en tests
- [ ] Operaciones cache básicas funcionan

**Related Requirements**: R3, R12

---

## Milestone 2: Common Modules

### Task 2.1: Implement JWT Token Provider (auth-common)

**Descripción**: Generación y validación de tokens JWT.

**Enfoque TDD**:
1. **Red Phase**:
   - Test generación token con userId válido
   - Test extracción userId desde token
   - Test validación token expirado retorna false
   - Property Test: round-trip userId

2. **Green Phase**:
   - Crear `JwtTokenProvider`
   - Implementar generación con jjwt
   - Implementar validación

3. **Refactor Phase**:
   - Extraer configuración a properties

**Acceptance Criteria**:
- [ ] Tokens JWT generados correctamente
- [ ] Validación detecta tokens expirados/inválidos
- [ ] Configuración externalizada

**Related Requirements**: R4, R11

---

### Task 2.2: Implement Security Filter Chain (security-common)

**Descripción**: Configuración de Spring Security con JWT.

**Enfoque TDD**:
1. **Red Phase**:
   - Test endpoints públicos accesibles sin auth
   - Test endpoints protegidos requieren token
   - Test token inválido retorna 401
   
2. **Green Phase**:
   - Crear `SecurityConfig`
   - Crear `JwtAuthenticationFilter`
   - Configurar rutas públicas vs protegidas

**Acceptance Criteria**:
- [ ] Endpoints públicos accesibles
- [ ] Endpoints protegidos requieren JWT válido
- [ ] Respuestas 401/403 apropiadas

**Related Requirements**: R4, R11

---

### Task 2.3: Implement API Response Wrapper (common-utils)

**Descripción**: Formato estándar de respuestas API.

**Enfoque TDD**:
1. **Red Phase**:
   - Test success response format
   - Test error response format
   - Test validation error format
   
2. **Green Phase**:
   - Crear `ApiResponse<T>` record
   - Crear `ApiError` record
   - Crear `GlobalExceptionHandler`

**Acceptance Criteria**:
- [ ] Todas las respuestas siguen formato estándar
- [ ] Errores incluyen código y mensaje
- [ ] Errores de validación incluyen detalles por campo

**Related Requirements**: R11

---

### Task 2.4: Implement Domain-Ability-Permission Entities (authorization-common)

**Descripción**: Entidades para el modelo de autorización jerárquico.

**Enfoque TDD**:
1. **Red Phase**:
   - Test crear Domain persiste correctamente
   - Test crear Ability con permissions
   - Test crear Permission con resource:action
   - Test UserDomainAbility grant/revoke
   - Test UserDomainPermissionOverride

2. **Green Phase**:
   - Crear entidad `Domain`
   - Crear entidad `Ability` con relación M:N a Permission
   - Crear entidad `Permission`
   - Crear entidad `UserDomainAbility`
   - Crear entidad `UserDomainPermissionOverride`
   - Crear repositories

3. **Refactor Phase**:
   - Agregar índices para lookups rápidos
   - Documentar relaciones

**Acceptance Criteria**:
- [ ] Todas las entidades persisten correctamente
- [ ] Relaciones M:N funcionan (Ability ↔ Permission)
- [ ] Constraints únicos funcionan (user + domain + ability)
- [ ] Índices creados para performance

**Related Requirements**: R11

**Reference**: See [authorization.md](authorization.md) for full schema.

---

### Task 2.5: Implement DomainAuthorizationService (authorization-common)

**Descripción**: Servicio de autorización con lógica de resolución de permisos.

**Enfoque TDD**:
1. **Red Phase**:
   - Test `hasPermission` retorna true cuando ability lo incluye
   - Test `hasPermission` retorna false cuando no hay ability
   - Test `hasPermission` retorna false cuando domain diferente
   - Test override deny toma precedencia sobre ability grant
   - Test override allow agrega permiso sin ability
   - Test `hasAbility` funciona correctamente
   - Test `hasDomainAccess` funciona correctamente
   - Test `getPermissions` retorna set completo con overrides aplicados

2. **Green Phase**:
   - Crear `DomainAuthorizationService`
   - Implementar resolución: override → ability → deny

3. **Refactor Phase**:
   - Agregar caching de permisos por usuario/dominio
   - Optimizar queries con proyecciones

**Acceptance Criteria**:
- [ ] Resolución de permisos correcta
- [ ] Overrides funcionan (deny y allow)
- [ ] Domain isolation funciona
- [ ] Performance < 10ms para hasPermission

**Related Requirements**: R11

---

### Task 2.6: Implement Authorization Annotations (authorization-common)

**Descripción**: Anotaciones `@RequiresPermission` y `@RequiresAbility` con evaluador SpEL.

**Enfoque TDD**:
1. **Red Phase**:
   - Test `@RequiresPermission` permite acceso con permiso
   - Test `@RequiresPermission` retorna 403 sin permiso
   - Test `@RequiresPermission` retorna 403 en domain incorrecto
   - Test `@RequiresAbility` funciona correctamente
   - Test usuario no autenticado retorna 401

2. **Green Phase**:
   - Crear anotación `@RequiresPermission`
   - Crear anotación `@RequiresAbility`
   - Crear `DomainAuthorizationEvaluator` (@Component)
   - Integrar con `@PreAuthorize`

3. **Refactor Phase**:
   - Agregar soporte para domainCode desde path variable
   - Documentar uso

**Acceptance Criteria**:
- [ ] Anotaciones funcionan en controllers
- [ ] Domain extraído automáticamente de path
- [ ] 401 para no autenticados, 403 para sin permiso
- [ ] SpEL expressions funcionan

**Related Requirements**: R11

---

### Task 2.7: Seed Authorization Data

**Descripción**: Datos iniciales de domains, abilities y permissions.

**Enfoque TDD**:
1. **Red Phase**:
   - Test domain "main-store" existe después de seed
   - Test todas las abilities definidas existen
   - Test todas las permissions definidas existen
   - Test mappings ability→permission correctos

2. **Green Phase**:
   - Crear `DataSeeder` con `@PostConstruct` o Flyway migration
   - Insertar domain, abilities, permissions
   - Mapear abilities a permissions

**Acceptance Criteria**:
- [ ] Domain "main-store" creado
- [ ] 13 abilities creadas
- [ ] 40+ permissions creadas
- [ ] Mappings correctos

**Related Requirements**: R11

**Reference**: See [authorization.md](authorization.md) for seed data.

---

## Milestone 3: Catalog Module

### Task 3.1: Implement Category Entity and Repository

**Descripción**: Entidad Category con operaciones CRUD.

**Enfoque TDD**:
1. **Red Phase**:
   - Test crear categoría persiste en BD
   - Test encontrar por slug retorna categoría
   - Test listar todas retorna lista ordenada
   - Property Test: slug es único

2. **Green Phase**:
   - Crear entidad `Category`
   - Crear `CategoryRepository`

**Acceptance Criteria**:
- [ ] Categorías se persisten correctamente
- [ ] Búsqueda por slug funciona
- [ ] Slugs son únicos

**Related Requirements**: R1

---

### Task 3.2: Implement Product Entity and Repository

**Descripción**: Entidad Product con relación a Category.

**Enfoque TDD**:
1. **Red Phase**:
   - Test crear producto con categoría
   - Test encontrar por slug
   - Test filtrar por categoría
   - Test buscar por nombre/descripción
   - Test paginación funciona

2. **Green Phase**:
   - Crear entidad `Product`
   - Crear `ProductRepository` con queries custom

**Acceptance Criteria**:
- [ ] Productos se relacionan con categorías
- [ ] Búsqueda y filtrado funcionan
- [ ] Paginación implementada

**Related Requirements**: R1, R2

---

### Task 3.3: Implement Catalog Service

**Descripción**: Lógica de negocio para catálogo.

**Enfoque TDD**:
1. **Red Phase**:
   - Test listar productos paginados
   - Test filtrar por categoría
   - Test buscar por keyword
   - Test obtener detalle de producto
   - Test obtener productos relacionados

2. **Green Phase**:
   - Crear `ProductService`
   - Crear `CategoryService`
   - Implementar DTOs

**Acceptance Criteria**:
- [ ] Servicios implementan toda la lógica
- [ ] DTOs exponen datos apropiados
- [ ] Sin lógica en controladores

**Related Requirements**: R1, R2

---

### Task 3.4: Implement Catalog REST Controllers

**Descripción**: Endpoints REST para catálogo.

**Enfoque TDD**:
1. **Red Phase**:
   - Test GET /api/products retorna lista paginada
   - Test GET /api/products?categoryId=X filtra
   - Test GET /api/products?search=X busca
   - Test GET /api/products/{slug} retorna detalle
   - Test GET /api/categories retorna todas

2. **Green Phase**:
   - Crear `ProductController`
   - Crear `CategoryController`

**Acceptance Criteria**:
- [ ] Endpoints responden correctamente
- [ ] Query params funcionan
- [ ] Respuestas siguen formato estándar

**Related Requirements**: R1, R2

---

### Task 3.5: Implement Catalog Caching

**Descripción**: Cache de Redis para catálogo.

**Enfoque TDD**:
1. **Red Phase**:
   - Test primera llamada va a BD
   - Test segunda llamada usa cache
   - Test actualizar producto invalida cache
   
2. **Green Phase**:
   - Agregar `@Cacheable` annotations
   - Configurar cache manager
   - Implementar invalidación

**Acceptance Criteria**:
- [ ] Queries cacheados en Redis
- [ ] Cache se invalida en updates
- [ ] TTL configurado (5 min)

**Related Requirements**: R12

---

## Milestone 4: Customer Module

### Task 4.1: Implement Customer Entity and Repository

**Descripción**: Entidad Customer para usuarios registrados.

**Enfoque TDD**:
1. **Red Phase**:
   - Test crear customer con email único
   - Test encontrar por email
   - Test password se hashea
   
2. **Green Phase**:
   - Crear entidad `Customer`
   - Crear `CustomerRepository`

**Acceptance Criteria**:
- [ ] Emails son únicos
- [ ] Passwords hasheados con BCrypt

**Related Requirements**: R4

---

### Task 4.2: Implement Address Entity and Repository

**Descripción**: Entidad Address para direcciones de envío.

**Enfoque TDD**:
1. **Red Phase**:
   - Test crear address para customer
   - Test listar addresses de customer
   - Test marcar como default
   
2. **Green Phase**:
   - Crear entidad `Address`
   - Crear `AddressRepository`

**Acceptance Criteria**:
- [ ] Múltiples addresses por customer
- [ ] Solo una default address

**Related Requirements**: R5

---

### Task 4.3: Implement Registration Service

**Descripción**: Registro de nuevos usuarios.

**Enfoque TDD**:
1. **Red Phase**:
   - Test registro crea customer
   - Test email duplicado falla
   - Test email verification token generado
   - Test merge guest cart on register

2. **Green Phase**:
   - Crear `RegistrationService`
   - Crear `EmailVerificationService`

**Acceptance Criteria**:
- [ ] Registro exitoso
- [ ] Validación de email único
- [ ] Email verification funciona

**Related Requirements**: R4

---

### Task 4.4: Implement Authentication Service

**Descripción**: Login y gestión de sesiones.

**Enfoque TDD**:
1. **Red Phase**:
   - Test login válido retorna token
   - Test login inválido falla
   - Test password reset flow
   
2. **Green Phase**:
   - Crear `AuthenticationService`
   - Crear `PasswordResetService`

**Acceptance Criteria**:
- [ ] Login genera JWT
- [ ] Credenciales inválidas = 401
- [ ] Password reset funciona

**Related Requirements**: R4

---

### Task 4.5: Implement Customer REST Controllers

**Descripción**: Endpoints para auth y perfil.

**Enfoque TDD**:
1. **Red Phase**:
   - Test POST /api/auth/register
   - Test POST /api/auth/login
   - Test GET /api/customers/me
   - Test CRUD /api/customers/me/addresses
   - **Authorization tests**:
     - Test GET /api/customers/me sin auth retorna 401
     - Test GET /api/customers/me/addresses sin auth retorna 401
     - Test customer solo puede ver sus propias addresses (ownership)

2. **Green Phase**:
   - Crear `AuthController`
   - Crear `CustomerController`
   - Agregar `@RequiresPermission` annotations

**Acceptance Criteria**:
- [ ] Registro y login funcionan
- [ ] Perfil y addresses CRUD completo
- [ ] 401 para endpoints protegidos sin auth
- [ ] Ownership validation para addresses

**Related Requirements**: R4, R5, R11

---

## Milestone 5: Cart Module

### Task 5.1: Implement Cart Entity and Repository

**Descripción**: Entidad Cart con items.

**Enfoque TDD**:
1. **Red Phase**:
   - Test crear cart para customer
   - Test crear cart para session (guest)
   - Test agregar items
   
2. **Green Phase**:
   - Crear entidad `Cart`
   - Crear entidad `CartItem`
   - Crear repositories

**Acceptance Criteria**:
- [ ] Carts para users y guests
- [ ] Items relacionados a products

**Related Requirements**: R3

---

### Task 5.2: Implement Cart Service

**Descripción**: Lógica de carrito de compras.

**Enfoque TDD**:
1. **Red Phase**:
   - Test agregar producto al carrito
   - Test actualizar cantidad
   - Test remover item
   - Test validar stock disponible
   - Test calcular totales
   - Test merge guest cart a user

2. **Green Phase**:
   - Crear `CartService`
   - Implementar stock validation

**Acceptance Criteria**:
- [ ] CRUD de items funciona
- [ ] Validación de stock
- [ ] Merge de guest cart

**Related Requirements**: R3

---

### Task 5.3: Implement Cart Caching

**Descripción**: Cache de carritos en Redis.

**Enfoque TDD**:
1. **Red Phase**:
   - Test cart se cachea en Redis
   - Test modificación actualiza cache
   - Test expiration 24h
   
2. **Green Phase**:
   - Implementar Redis cache para carts
   - Configurar TTL

**Acceptance Criteria**:
- [ ] Carts cacheados
- [ ] Performance mejorado

**Related Requirements**: R3, R12

---

### Task 5.4: Implement Cart REST Controller

**Descripción**: Endpoints de carrito.

**Enfoque TDD**:
1. **Red Phase**:
   - Test GET /api/cart
   - Test POST /api/cart/items
   - Test PUT /api/cart/items/{id}
   - Test DELETE /api/cart/items/{id}
   - **Authorization tests**:
     - Test guest puede acceder cart con sessionId
     - Test customer puede acceder su propio cart
     - Test customer no puede acceder cart de otro (403)
     - Test modificar item de otro cart retorna 403

2. **Green Phase**:
   - Crear `CartController`
   - Agregar `@RequiresPermission` para cart operations
   - Implementar ownership validation

**Acceptance Criteria**:
- [ ] CRUD endpoints funcionan
- [ ] Soporta auth y guest
- [ ] Ownership validation para cart items
- [ ] 403 al acceder cart de otro usuario

**Related Requirements**: R3, R11

---

## Milestone 6: Order Module

### Task 6.1: Implement Order Entity and Repository

**Descripción**: Entidades Order y OrderItem.

**Enfoque TDD**:
1. **Red Phase**:
   - Test crear order desde cart
   - Test order number único
   - Test order items inmutables
   
2. **Green Phase**:
   - Crear entidad `Order`
   - Crear entidad `OrderItem`
   - Crear repositories

**Acceptance Criteria**:
- [ ] Orders creados desde carts
- [ ] Order numbers únicos
- [ ] Items preservan precio al momento

**Related Requirements**: R6

---

### Task 6.2: Implement Checkout Service

**Descripción**: Proceso de checkout.

**Enfoque TDD**:
1. **Red Phase**:
   - Test validar cart antes de checkout
   - Test crear order reserva stock
   - Test stock insuficiente falla
   - Test calcular shipping cost
   
2. **Green Phase**:
   - Crear `CheckoutService`
   - Implementar stock reservation

**Acceptance Criteria**:
- [ ] Validación pre-checkout
- [ ] Reserva de stock atómica
- [ ] Cálculo de shipping

**Related Requirements**: R6

---

### Task 6.3: Implement Order Service

**Descripción**: Gestión de órdenes.

**Enfoque TDD**:
1. **Red Phase**:
   - Test obtener órdenes de customer
   - Test obtener detalle de orden
   - Test cancelar orden (solo pending/paid)
   - Test transiciones de estado válidas
   
2. **Green Phase**:
   - Crear `OrderService`
   - Implementar state machine

**Acceptance Criteria**:
- [ ] CRUD de órdenes
- [ ] Estado machine válido
- [ ] Cancelación restaura stock

**Related Requirements**: R8

---

### Task 6.4: Implement Order Notifications

**Descripción**: Notificaciones de cambio de estado.

**Enfoque TDD**:
1. **Red Phase**:
   - Test estado change envia email
   - Test email contiene info correcta
   
2. **Green Phase**:
   - Crear `OrderNotificationService`
   - Integrar SendGrid

**Acceptance Criteria**:
- [ ] Emails enviados en cambios de estado
- [ ] Templates correctos

**Related Requirements**: R8

---

### Task 6.5: Implement Order REST Controllers

**Descripción**: Endpoints de checkout y órdenes.

**Enfoque TDD**:
1. **Red Phase**:
   - Test POST /api/checkout
   - Test GET /api/orders
   - Test GET /api/orders/{orderNumber}
   - Test POST /api/orders/{orderNumber}/cancel
   - **Authorization tests**:
     - Test checkout sin auth retorna 401
     - Test GET /api/orders solo retorna órdenes propias
     - Test GET /api/orders/{orderNumber} de otro usuario retorna 403
     - Test cancelar orden de otro usuario retorna 403
     - Test customer sin permiso `orders:cancel-own` no puede cancelar

2. **Green Phase**:
   - Crear `CheckoutController`
   - Crear `OrderController`
   - Agregar `@RequiresPermission` annotations
   - Implementar ownership validation

**Acceptance Criteria**:
- [ ] Checkout flow completo
- [ ] Order management endpoints
- [ ] 401 para endpoints sin auth
- [ ] 403 para acceso a órdenes de otros
- [ ] Ownership validation funciona

**Related Requirements**: R6, R8, R11

---

## Milestone 7: Payment Module

### Task 7.1: Implement Payment Entity and Repository

**Descripción**: Entidad Payment para transacciones.

**Enfoque TDD**:
1. **Red Phase**:
   - Test crear payment para order
   - Test actualizar status
   
2. **Green Phase**:
   - Crear entidad `Payment`
   - Crear `PaymentRepository`

**Acceptance Criteria**:
- [ ] Payments relacionados a orders
- [ ] Estados de pago trackeable

**Related Requirements**: R7

---

### Task 7.2: Implement Stripe Integration

**Descripción**: Integración con Stripe para pagos.

**Enfoque TDD**:
1. **Red Phase**:
   - Test crear payment intent
   - Test confirmar pago exitoso
   - Test manejar pago fallido
   - Test webhook signature validation
   
2. **Green Phase**:
   - Crear `StripePaymentGateway`
   - Implementar webhook handler

**Acceptance Criteria**:
- [ ] Payment intents creados
- [ ] Webhooks procesados
- [ ] Tokenización (no raw cards)

**Related Requirements**: R7

---

### Task 7.3: Implement Payment Service

**Descripción**: Orquestación de pagos.

**Enfoque TDD**:
1. **Red Phase**:
   - Test iniciar pago crea intent
   - Test pago exitoso actualiza order
   - Test refund procesa correctamente
   
2. **Green Phase**:
   - Crear `PaymentService`
   - Orquestar con OrderService

**Acceptance Criteria**:
- [ ] Flow de pago completo
- [ ] Refunds funcionan
- [ ] Order status sincronizado

**Related Requirements**: R7

---

### Task 7.4: Implement Payment REST Controller

**Descripción**: Endpoints de pago.

**Enfoque TDD**:
1. **Red Phase**:
   - Test POST /api/payments/intent
   - Test POST /api/payments/confirm
   - Test POST /api/payments/webhook

2. **Green Phase**:
   - Crear `PaymentController`

**Acceptance Criteria**:
- [ ] Payment flow vía API
- [ ] Webhook endpoint seguro

**Related Requirements**: R7

---

## Milestone 8: Admin Module

### Task 8.1: Implement Admin Product Management

**Descripción**: CRUD de productos para admins.

**Enfoque TDD**:
1. **Red Phase**:
   - Test crear producto (admin only)
   - Test actualizar producto
   - Test actualizar stock
   - Test toggle active status
   - **Authorization tests (Domain-Ability-Permission)**:
     - Test sin auth retorna 401
     - Test customer sin ability retorna 403
     - Test user con `manage-inventory` ability puede crear producto
     - Test user con `view-inventory` ability NO puede crear producto (403)
     - Test user con permiso en domain diferente retorna 403
     - Test `products:create` permission requerido para POST
     - Test `inventory:update-stock` permission requerido para stock update

2. **Green Phase**:
   - Crear `AdminProductService`
   - Crear `AdminProductController` con path `/api/domains/{domainCode}/admin/products`
   - Agregar `@RequiresPermission` annotations por endpoint

**Acceptance Criteria**:
- [ ] Solo usuarios con permisos apropiados acceden
- [ ] CRUD completo de productos
- [ ] Domain isolation funciona
- [ ] 401/403 responses correctos

**Related Requirements**: R9, R11

---

### Task 8.2: Implement Admin Order Management

**Descripción**: Gestión de órdenes para admins.

**Enfoque TDD**:
1. **Red Phase**:
   - Test listar órdenes pendientes
   - Test actualizar estado
   - Test agregar tracking number
   - **Authorization tests (Domain-Ability-Permission)**:
     - Test sin auth retorna 401
     - Test customer sin ability retorna 403
     - Test user con `process-orders` ability puede ver y actualizar estado
     - Test user con `process-orders` NO puede cancelar/refundar (403)
     - Test user con `manage-orders` ability puede cancelar y refundar
     - Test `orders:update-status` permission requerido para cambiar estado
     - Test `orders:refund` permission requerido para refunds
     - Test domain isolation (403 para domain incorrecto)

2. **Green Phase**:
   - Crear `AdminOrderService`
   - Crear `AdminOrderController` con path `/api/domains/{domainCode}/admin/orders`
   - Agregar `@RequiresPermission` annotations por endpoint

**Acceptance Criteria**:
- [ ] Fulfillment workflow completo
- [ ] Tracking integrado
- [ ] Permission-based access control
- [ ] Domain isolation funciona
- [ ] 401/403 responses correctos

**Related Requirements**: R10, R11

---

### Task 8.3: Implement Low Stock Alerts

**Descripción**: Alertas cuando stock bajo umbral.

**Enfoque TDD**:
1. **Red Phase**:
   - Test stock bajo threshold genera alerta
   - Test alerta enviada a admin
   
2. **Green Phase**:
   - Crear `InventoryAlertService`
   - Implementar notificaciones

**Acceptance Criteria**:
- [ ] Alertas automáticas
- [ ] Configuración de thresholds

**Related Requirements**: R9

---

## Milestone 9: Frontend (React)

### Task 9.1: Setup React Project

**Descripción**: Inicializar frontend con React + TypeScript.

**Acceptance Criteria**:
- [ ] Vite + React 18 + TypeScript
- [ ] TailwindCSS configurado
- [ ] React Router configurado

---

### Task 9.2: Implement Product Catalog UI

**Descripción**: Páginas de catálogo y detalle.

**Acceptance Criteria**:
- [ ] Grid de productos con filtros
- [ ] Página de detalle de producto
- [ ] Búsqueda funcional

---

### Task 9.3: Implement Cart UI

**Descripción**: Carrito de compras interactivo.

**Acceptance Criteria**:
- [ ] Agregar/remover items
- [ ] Actualizar cantidades
- [ ] Resumen de totales

---

### Task 9.4: Implement Checkout Flow UI

**Descripción**: Proceso de checkout completo.

**Acceptance Criteria**:
- [ ] Selección de dirección
- [ ] Integración Stripe Elements
- [ ] Confirmación de orden

---

### Task 9.5: Implement Customer Account UI

**Descripción**: Páginas de cuenta de usuario.

**Acceptance Criteria**:
- [ ] Login/Register
- [ ] Perfil y direcciones
- [ ] Historial de órdenes

---

## Milestone 10: Security & Performance

### Task 10.1: Implement Rate Limiting

**Descripción**: Rate limiting en endpoints sensibles.

**Acceptance Criteria**:
- [ ] 10 req/min en auth endpoints
- [ ] 429 response cuando excede

**Related Requirements**: R11

---

### Task 10.2: Implement Audit Logging

**Descripción**: Logging de acciones administrativas.

**Acceptance Criteria**:
- [ ] Todas las acciones admin loggeadas
- [ ] Logs incluyen user, action, timestamp

**Related Requirements**: R11

---

### Task 10.3: Performance Optimization

**Descripción**: Optimizaciones de performance.

**Acceptance Criteria**:
- [ ] Queries optimizados con índices
- [ ] Connection pooling configurado
- [ ] Catalog queries < 200ms p95

**Related Requirements**: R12

---

## Summary

| Milestone | Tasks | Focus |
|-----------|-------|-------|
| 1 | 1.1-1.3 | Infrastructure |
| 2 | 2.1-2.7 | Common Modules + **Authorization** |
| 3 | 3.1-3.5 | Catalog |
| 4 | 4.1-4.5 | Customer |
| 5 | 5.1-5.4 | Cart |
| 6 | 6.1-6.5 | Order |
| 7 | 7.1-7.4 | Payment |
| 8 | 8.1-8.3 | Admin |
| 9 | 9.1-9.5 | Frontend |
| 10 | 10.1-10.3 | Security & Perf |

**Total Tasks**: 42 (4 new authorization tasks)
**Estimated Duration**: 7-9 weeks for MVP

### Authorization Tasks Added (Milestone 2)

| Task | Description | Effort |
|------|-------------|--------|
| 2.4 | Domain-Ability-Permission Entities | 1 day |
| 2.5 | DomainAuthorizationService | 1.5 days |
| 2.6 | Authorization Annotations | 1 day |
| 2.7 | Seed Authorization Data | 0.5 days |

### Authorization Tests Added to Existing Tasks

| Task | Authorization Tests Added |
|------|--------------------------|
| 4.5 | Customer profile ownership validation |
| 5.4 | Cart ownership validation |
| 6.5 | Order ownership + permission checks |
| 8.1 | Admin product domain-ability-permission tests |
| 8.2 | Admin order domain-ability-permission tests |

**Reference**: See [authorization.md](authorization.md) for full authorization architecture.
