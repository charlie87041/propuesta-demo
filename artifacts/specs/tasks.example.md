# Implementation Plan: Collaborative Document Management System

## Overview

Este plan de implementación sigue un enfoque **Test-Driven Development (TDD)** estricto, donde cada tarea sigue el ciclo Red-Green-Refactor. Las tareas están organizadas en milestones lógicos que construyen el sistema incrementalmente, comenzando con la infraestructura compartida y avanzando hacia los servicios específicos.

**Tecnologías**: Spring Boot 3.2.x, Java 17+, Gradle 8.x, PostgreSQL 15+, jqwik (property-based testing), JUnit 5, Mockito, TestContainers

**Principios TDD**:
1. **Red Phase**: Escribir tests que fallen primero
2. **Green Phase**: Implementar código mínimo para pasar tests
3. **Refactor Phase**: Mejorar el código manteniendo tests verdes

---

## Milestone 1: Project Setup & Infrastructure

### Task 1.1: Initialize Gradle Multi-Module Project

**Descripción**: Configurar la estructura base del proyecto con Gradle multi-módulo, incluyendo configuración de dependencias compartidas y plugins.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Verificar que el build.gradle raíz existe y contiene configuración de subproyectos
     - Verificar que settings.gradle incluye todos los módulos esperados
   
2. **Green Phase - Implement Minimum Code**:
   - Crear estructura de directorios: `common/`, `workspace-service/`, `project-service/`
   - Crear `build.gradle` raíz con configuración común (Spring Boot, Java 17, dependencias)
   - Crear `settings.gradle` con inclusión de módulos
   - Crear `build.gradle` para cada submódulo

3. **Refactor Phase**:
   - Extraer versiones de dependencias a variables
   - Configurar repositorios Maven Central

**Acceptance Criteria**:
- [ ] Estructura de directorios multi-módulo creada
- [ ] `./gradlew build` ejecuta exitosamente
- [ ] Todos los módulos se compilan sin errores

**Related Requirements**: 17.1 (Data Persistence)


### Task 1.2: Configure PostgreSQL with TestContainers

**Descripción**: Configurar TestContainers para pruebas de integración con PostgreSQL, estableciendo la base para tests con base de datos real.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Integration Tests:
     - Test que verifica conexión a PostgreSQL container
     - Test que verifica creación de esquema básico
   
2. **Green Phase - Implement Minimum Code**:
   - Agregar dependencias de TestContainers en `build.gradle`
   - Crear clase base `AbstractIntegrationTest` con configuración de container
   - Configurar `application-test.yml` con propiedades de conexión

3. **Refactor Phase**:
   - Optimizar configuración de container para reutilización entre tests
   - Configurar singleton container para mejorar performance

**Acceptance Criteria**:
- [ ] TestContainers inicia PostgreSQL correctamente
- [ ] Tests de integración pueden conectarse a la BD
- [ ] Container se limpia automáticamente después de tests

**Related Requirements**: 17.1, 17.2 (Data Persistence and Integrity)

---

## Milestone 2: Common Modules (Shared Infrastructure)

### Task 2.1: Implement JWT Token Provider (auth-common)

**Descripción**: Implementar generación y validación de tokens JWT para autenticación entre servicios.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test generación de token con userId y username válidos
     - Test extracción de userId desde token válido
     - Test extracción de username desde token válido
     - Test validación de token válido retorna true
     - Test validación de token expirado retorna false
     - Test validación de token malformado retorna false
   - Property Tests:
     - **Property 1**: *For any* valid userId and username, generating a token and extracting userId should return the original userId (round-trip)
     - **Property 2**: *For any* valid userId and username, generating a token and extracting username should return the original username (round-trip)
   
2. **Green Phase - Implement Minimum Code**:
   - Crear clase `JwtTokenProvider` con métodos básicos
   - Implementar generación de token usando biblioteca JWT (jjwt)
   - Implementar extracción de claims
   - Implementar validación básica

3. **Refactor Phase**:
   - Extraer configuración (secret, expiration) a properties
   - Mejorar manejo de excepciones
   - Agregar logging

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Todos los property tests pasan (100+ iteraciones)
- [ ] Tokens generados son válidos según estándar JWT
- [ ] Tokens expirados son rechazados correctamente

**Related Requirements**: 16.1 (User Authentication)
**Related Properties**: Property 9 (Project metadata round-trip - patrón similar)


### Task 2.2: Implement Security Configuration (security-common)

**Descripción**: Configurar Spring Security con filtros JWT y políticas de autorización.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test que SecurityFilterChain está configurado
     - Test que PasswordEncoder es BCrypt
     - Test que endpoints públicos no requieren autenticación
     - Test que endpoints protegidos requieren token JWT
   - Integration Tests:
     - Test request sin token a endpoint protegido retorna 401
     - Test request con token válido a endpoint protegido retorna 200
     - Test request con token inválido retorna 401
   
2. **Green Phase - Implement Minimum Code**:
   - Crear `SecurityConfig` con configuración básica
   - Implementar `JwtAuthenticationFilter` para validar tokens
   - Configurar `PasswordEncoder` con BCrypt
   - Definir rutas públicas vs protegidas

3. **Refactor Phase**:
   - Extraer configuración de rutas a constantes
   - Mejorar manejo de errores de autenticación
   - Agregar CORS configuration

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Todos los tests de integración pasan
- [ ] Endpoints protegidos rechazan requests sin token
- [ ] Tokens válidos permiten acceso a endpoints protegidos

**Related Requirements**: 16.1, 16.2, 16.3 (Authentication and Authorization)

### Task 2.3: Implement Common Utilities (common-utils)

**Descripción**: Crear utilidades compartidas para validación, manejo de errores y DTOs comunes.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test `ErrorResponse` serializa correctamente a JSON
     - Test `ValidationException` contiene mensajes de error
     - Test utilidades de fecha/hora funcionan correctamente
   - Property Tests:
     - **Property**: *For any* ErrorResponse, serializar y deserializar debe retornar objeto equivalente (round-trip)
   
2. **Green Phase - Implement Minimum Code**:
   - Crear clase `ErrorResponse` para respuestas de error estandarizadas
   - Crear excepciones custom: `ValidationException`, `AuthorizationException`, `NotFoundException`
   - Crear `GlobalExceptionHandler` con `@ControllerAdvice`
   - Crear utilidades de fecha/hora

3. **Refactor Phase**:
   - Mejorar mensajes de error para ser más descriptivos
   - Agregar builder pattern para ErrorResponse
   - Documentar excepciones con Javadoc

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Excepciones custom se lanzan y capturan correctamente
- [ ] ErrorResponse tiene formato consistente

**Related Requirements**: 17.1 (Data Persistence)

---

## Milestone 3: Workspace Service - Core

### Task 3.1: Implement User Entity and Repository

**Descripción**: Crear entidad User con JPA y repository para persistencia.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test que User entity tiene todos los campos requeridos
     - Test que username debe ser único (constraint violation)
     - Test que email debe ser único (constraint violation)
   - Integration Tests:
     - Test guardar User en BD y recuperarlo
     - Test buscar User por username
     - Test buscar User por email
     - Test intentar guardar User con username duplicado lanza excepción
   - Property Tests:
     - **Property**: *For any* User con datos válidos, guardar y recuperar debe retornar User equivalente (round-trip)
   
2. **Green Phase - Implement Minimum Code**:
   - Crear entidad `User` con anotaciones JPA
   - Crear `UserRepository` extendiendo `JpaRepository`
   - Agregar métodos de búsqueda: `findByUsername`, `findByEmail`, `existsByUsername`, `existsByEmail`

3. **Refactor Phase**:
   - Agregar índices en username y email para performance
   - Implementar `equals()` y `hashCode()` basados en ID
   - Agregar validaciones con Bean Validation

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Todos los tests de integración pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Constraints de unicidad funcionan correctamente

**Related Requirements**: 16.1 (User Authentication)


### Task 3.2: Implement UserService with Registration and Authentication

**Descripción**: Implementar lógica de negocio para registro de usuarios y autenticación.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test registro de usuario con datos válidos crea User
     - Test registro con username existente lanza excepción
     - Test registro con email existente lanza excepción
     - Test autenticación con credenciales válidas retorna token
     - Test autenticación con password incorrecto lanza excepción
     - Test autenticación con usuario inexistente lanza excepción
   - Property Tests:
     - **Property**: *For any* username y password válidos, registrar usuario y luego autenticar debe retornar token válido
   
2. **Green Phase - Implement Minimum Code**:
   - Crear `UserService` con métodos `registerUser` y `authenticateUser`
   - Implementar hash de password con `PasswordEncoder`
   - Implementar validación de credenciales
   - Integrar con `JwtTokenProvider` para generar tokens

3. **Refactor Phase**:
   - Extraer validaciones a métodos privados
   - Mejorar mensajes de error
   - Agregar logging de eventos de seguridad

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Passwords se almacenan hasheados (nunca en texto plano)
- [ ] Tokens JWT se generan correctamente

**Related Requirements**: 16.1 (User Authentication)

### Task 3.3: Implement UserController with REST Endpoints

**Descripción**: Crear endpoints REST para registro, login y consulta de usuarios.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Integration Tests:
     - Test POST /api/users/register con datos válidos retorna 201
     - Test POST /api/users/register con username duplicado retorna 400
     - Test POST /api/users/login con credenciales válidas retorna 200 y token
     - Test POST /api/users/login con credenciales inválidas retorna 401
     - Test GET /api/users/me con token válido retorna usuario actual
     - Test GET /api/users/me sin token retorna 401
   - Contract Tests:
     - Test que response de /register cumple contrato JSON esperado
     - Test que response de /login contiene campo "token"
   
2. **Green Phase - Implement Minimum Code**:
   - Crear `UserController` con endpoints básicos
   - Implementar DTOs: `RegisterRequest`, `LoginRequest`, `LoginResponse`, `UserDTO`
   - Implementar mapeo de entidades a DTOs
   - Configurar validación de requests con `@Valid`

3. **Refactor Phase**:
   - Extraer mapeo a clase `UserMapper`
   - Mejorar documentación de API con comentarios
   - Agregar validaciones adicionales (formato email, longitud password)

**Acceptance Criteria**:
- [ ] Todos los tests de integración pasan
- [ ] Contract tests pasan
- [ ] Endpoints retornan códigos HTTP correctos
- [ ] Validación de input funciona correctamente

**Related Requirements**: 16.1 (User Authentication)

---

## Milestone 4: Workspace Service - Workspace Management

### Task 4.1: Implement Workspace Entity and Repository

**Descripción**: Crear entidad Workspace con relación a User (owner) y repository.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test que Workspace entity tiene campos requeridos
     - Test que ownerId no puede ser null
   - Integration Tests:
     - Test guardar Workspace en BD y recuperarlo
     - Test buscar Workspaces por ownerId
   - Property Tests:
     - **Property 1**: *For any* Workspace con datos válidos, guardar y recuperar debe retornar Workspace equivalente (round-trip)
     - **Property 2**: *For any* userId, crear workspace debe asignar ese userId como ownerId (**Validates: Property 1**)
   
2. **Green Phase - Implement Minimum Code**:
   - Crear entidad `Workspace` con anotaciones JPA
   - Crear `WorkspaceRepository` extendiendo `JpaRepository`
   - Agregar método `findByOwnerId`

3. **Refactor Phase**:
   - Agregar índice en ownerId
   - Implementar `equals()` y `hashCode()`
   - Agregar timestamps con `@CreationTimestamp` y `@UpdateTimestamp`

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Todos los tests de integración pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Cobertura de código > 80%

**Related Requirements**: 1.1 (Workspace Management)
**Related Properties**: Property 1 (Workspace ownership on creation)


### Task 4.2: Implement WorkspaceMember Entity and Repository

**Descripción**: Crear entidad WorkspaceMember para gestionar membresías de usuarios en workspaces.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test que WorkspaceMember tiene workspaceId y userId
     - Test que combinación workspaceId+userId debe ser única
   - Integration Tests:
     - Test guardar WorkspaceMember en BD
     - Test buscar miembros por workspaceId
     - Test buscar workspaces por userId
     - Test intentar agregar mismo usuario dos veces lanza excepción
   - Property Tests:
     - **Property 2**: *For any* workspace y userId, agregar usuario como miembro debe hacer que el usuario aparezca en la lista de miembros (**Validates: Property 2**)
   
2. **Green Phase - Implement Minimum Code**:
   - Crear entidad `WorkspaceMember` con anotaciones JPA
   - Crear `WorkspaceMemberRepository`
   - Agregar métodos: `findByWorkspaceId`, `findByUserId`, `existsByWorkspaceIdAndUserId`
   - Agregar constraint único en (workspaceId, userId)

3. **Refactor Phase**:
   - Agregar índices compuestos para queries frecuentes
   - Implementar método de utilidad `isMember(workspaceId, userId)`

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Todos los tests de integración pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Constraint de unicidad funciona correctamente

**Related Requirements**: 1.2, 1.3 (Workspace Member Management)
**Related Properties**: Property 2 (Member addition to workspace)

### Task 4.3: Implement WorkspaceService

**Descripción**: Implementar lógica de negocio para creación y gestión de workspaces.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test crear workspace asigna ownerId correctamente
     - Test agregar miembro a workspace válido
     - Test agregar miembro a workspace inexistente lanza excepción
     - Test remover miembro de workspace
     - Test validar ownership con owner válido no lanza excepción
     - Test validar ownership con no-owner lanza excepción
   - Property Tests:
     - **Property 1**: *For any* userId y nombre de workspace, crear workspace debe asignar userId como owner (**Validates: Property 1**)
     - **Property 3**: *For any* workspace con proyectos, agregar usuario como miembro debe permitir al usuario ver todos los proyectos (**Validates: Property 3** - se verificará en integración con Project Service)
   
2. **Green Phase - Implement Minimum Code**:
   - Crear `WorkspaceService` con métodos CRUD
   - Implementar `createWorkspace` que crea workspace y agrega owner como miembro
   - Implementar `addMember` y `removeMember`
   - Implementar `validateOwnership` para verificar permisos

3. **Refactor Phase**:
   - Extraer validaciones a métodos privados
   - Agregar transacciones con `@Transactional`
   - Mejorar manejo de excepciones

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Transacciones se manejan correctamente
- [ ] Validaciones de ownership funcionan

**Related Requirements**: 1.1, 1.2, 1.3 (Workspace Management)
**Related Properties**: Property 1, 2, 3

### Task 4.4: Implement WorkspaceController

**Descripción**: Crear endpoints REST para gestión de workspaces.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Integration Tests:
     - Test POST /api/workspaces crea workspace y retorna 201
     - Test GET /api/workspaces/{id} retorna workspace existente
     - Test GET /api/workspaces retorna workspaces del usuario autenticado
     - Test POST /api/workspaces/{id}/members agrega miembro
     - Test DELETE /api/workspaces/{id}/members/{userId} remueve miembro
     - Test operaciones sin autenticación retornan 401
     - Test operaciones sin ownership retornan 403
   - Contract Tests:
     - Test que response de crear workspace cumple contrato
     - Test que lista de workspaces cumple contrato
   
2. **Green Phase - Implement Minimum Code**:
   - Crear `WorkspaceController` con endpoints
   - Implementar DTOs: `CreateWorkspaceRequest`, `WorkspaceDTO`, `InviteMemberRequest`
   - Implementar mapeo de entidades a DTOs
   - Configurar autorización con `@PreAuthorize`

3. **Refactor Phase**:
   - Extraer mapeo a `WorkspaceMapper`
   - Mejorar mensajes de error
   - Agregar paginación para lista de workspaces

**Acceptance Criteria**:
- [ ] Todos los tests de integración pasan
- [ ] Contract tests pasan
- [ ] Autorización funciona correctamente
- [ ] Códigos HTTP son apropiados

**Related Requirements**: 1.1, 1.2, 1.3, 1.5 (Workspace Management)

---

## Milestone 5: Project Service - Core & State Management

### Task 5.1: Implement Project Entity and Repository

**Descripción**: Crear entidad Project con estados, versionado optimista y repository.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test que Project entity tiene todos los campos requeridos
     - Test que estado inicial es CREATION
     - Test que workspaceId no puede ser null
   - Integration Tests:
     - Test guardar Project en BD y recuperarlo
     - Test buscar Projects por workspaceId
     - Test buscar Projects por authorId
     - Test optimistic locking previene actualizaciones concurrentes
   - Property Tests:
     - **Property 6**: *For any* authorId y workspaceId, crear proyecto debe asignar authorId como owner (**Validates: Property 6**)
     - **Property 8**: *For any* proyecto recién creado, el estado debe ser CREATION (**Validates: Property 8**)
     - **Property 9**: *For any* proyecto con metadata, guardar y recuperar debe retornar metadata idéntica (**Validates: Property 9**)
     - **Property 10**: *For any* conjunto de proyectos creados, todos los IDs deben ser únicos (**Validates: Property 10**)
   
2. **Green Phase - Implement Minimum Code**:
   - Crear entidad `Project` con anotaciones JPA
   - Crear enum `ProjectState` (CREATION, EDITING, CONSULTATION, PUBLISHED)
   - Crear `ProjectRepository` extendiendo `JpaRepository`
   - Agregar `@Version` para optimistic locking
   - Agregar métodos: `findByWorkspaceId`, `findByAuthorId`

3. **Refactor Phase**:
   - Agregar índices en workspaceId y authorId
   - Implementar `equals()` y `hashCode()`
   - Agregar validaciones con Bean Validation

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Todos los tests de integración pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Optimistic locking funciona correctamente

**Related Requirements**: 2.1, 2.2, 2.3, 2.4, 2.5 (Project Creation)
**Related Properties**: Property 6, 8, 9, 10


### Task 5.2: Implement StateTransition Entity and Repository

**Descripción**: Crear entidad para registrar transiciones de estado de proyectos.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test que StateTransition tiene fromState, toState, performedBy
   - Integration Tests:
     - Test guardar StateTransition en BD
     - Test buscar transiciones por projectId en orden cronológico
   - Property Tests:
     - **Property 18**: *For any* transición de estado, debe registrarse timestamp (**Validates: Property 18**)
   
2. **Green Phase - Implement Minimum Code**:
   - Crear entidad `StateTransition` con anotaciones JPA
   - Crear `StateTransitionRepository`
   - Agregar método `findByProjectIdOrderByTransitionedAtAsc`

3. **Refactor Phase**:
   - Agregar índice en projectId
   - Agregar método de utilidad para obtener última transición

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Todos los tests de integración pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Timestamps se registran automáticamente

**Related Requirements**: 5.6 (State Transition Timestamp)
**Related Properties**: Property 18

### Task 5.3: Implement ProjectService with State Machine

**Descripción**: Implementar lógica de negocio para gestión de proyectos y máquina de estados.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test crear proyecto con datos válidos
     - Test crear proyecto sin nombre lanza excepción
     - Test transición CREATION → EDITING válida
     - Test transición EDITING → CONSULTATION válida
     - Test transición CONSULTATION → PUBLISHED válida
     - Test transición PUBLISHED → EDITING válida (nuevo ciclo)
     - Test transición CREATION → CONSULTATION inválida lanza excepción
     - Test transición por no-author lanza excepción
     - Test actualizar contenido en CREATION válido
     - Test actualizar contenido en EDITING lanza excepción (solo editores)
   - Property Tests:
     - **Property 17**: *For any* proyecto, transiciones válidas (CREATION→EDITING→CONSULTATION→PUBLISHED) deben suceder; saltar estados debe fallar (**Validates: Property 17**)
     - **Property 20**: *For any* proyecto en PUBLISHED, transicionar a EDITING debe iniciar nuevo ciclo (**Validates: Property 20**)
     - **Property 16**: *For any* proyecto en CREATION, author puede modificar contenido; en otros estados debe fallar (**Validates: Property 16**)
   
2. **Green Phase - Implement Minimum Code**:
   - Crear `ProjectService` con métodos CRUD
   - Implementar `createProject` que inicializa en CREATION
   - Implementar `transitionState` con validación de transiciones válidas
   - Implementar validación de author con `validateAuthorAccess`
   - Implementar `updateContent` con validación de estado
   - Registrar StateTransition en cada transición

3. **Refactor Phase**:
   - Extraer lógica de validación de transiciones a clase `StateTransitionValidator`
   - Agregar transacciones con `@Transactional`
   - Mejorar mensajes de error con estado actual y transición intentada

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Máquina de estados valida transiciones correctamente
- [ ] Solo authors pueden transicionar estados

**Related Requirements**: 2.1, 2.2, 2.3, 4.1, 4.3, 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 15.1 (Project Creation and State Transitions)
**Related Properties**: Property 6, 7, 8, 9, 16, 17, 18, 20

### Task 5.4: Implement ProjectController

**Descripción**: Crear endpoints REST para gestión de proyectos.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Integration Tests:
     - Test POST /api/projects crea proyecto y retorna 201
     - Test POST /api/projects sin nombre retorna 400
     - Test GET /api/projects/{id} retorna proyecto existente
     - Test GET /api/projects/workspace/{workspaceId} retorna proyectos del workspace
     - Test PUT /api/projects/{id}/state transiciona estado válido
     - Test PUT /api/projects/{id}/state con transición inválida retorna 409
     - Test PUT /api/projects/{id}/content actualiza contenido
     - Test operaciones sin autenticación retornan 401
     - Test operaciones sin permisos retornan 403
   - Contract Tests:
     - Test que response de crear proyecto cumple contrato
     - Test que lista de proyectos cumple contrato
   
2. **Green Phase - Implement Minimum Code**:
   - Crear `ProjectController` con endpoints
   - Implementar DTOs: `CreateProjectRequest`, `ProjectDTO`, `StateTransitionRequest`, `UpdateContentRequest`
   - Implementar mapeo de entidades a DTOs
   - Configurar autorización

3. **Refactor Phase**:
   - Extraer mapeo a `ProjectMapper`
   - Agregar paginación para lista de proyectos
   - Mejorar documentación de API

**Acceptance Criteria**:
- [ ] Todos los tests de integración pasan
- [ ] Contract tests pasan
- [ ] Validación de estado funciona
- [ ] Autorización funciona correctamente

**Related Requirements**: 2.1, 2.2, 2.3, 4.1, 5.1, 5.2, 5.3, 16.4 (Project Management)

---

## Milestone 6: Project Service - Roles & Content

### Task 6.1: Implement ProjectRole Entity and Repository

**Descripción**: Crear entidad ProjectRole para asignación de roles (EDITOR, CONSULTANT) a usuarios.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test que ProjectRole tiene projectId, userId, roleType
     - Test que combinación (projectId, userId, roleType) debe ser única
   - Integration Tests:
     - Test guardar ProjectRole en BD
     - Test buscar roles por projectId
     - Test buscar roles por userId
     - Test buscar roles específicos por projectId y userId
     - Test intentar asignar mismo rol dos veces lanza excepción
   - Property Tests:
     - **Property 11**: *For any* workspace member asignado rol EDITOR/CONSULTANT, debe tener permisos correspondientes (**Validates: Property 11**)
     - **Property 12**: *For any* usuario con rol, remover rol debe revocar permisos (round-trip: assign→verify→remove→verify absence) (**Validates: Property 12**)
     - **Property 13**: *For any* usuario en workspace, roles en un proyecto no afectan roles en otros proyectos (**Validates: Property 13**)
     - **Property 14**: *For any* proyecto con roles asignados, query debe retornar exactamente los roles asignados (**Validates: Property 14**)
   
2. **Green Phase - Implement Minimum Code**:
   - Crear enum `RoleType` (AUTHOR, EDITOR, CONSULTANT)
   - Crear entidad `ProjectRole` con anotaciones JPA
   - Crear `ProjectRoleRepository`
   - Agregar constraint único en (projectId, userId, roleType)
   - Agregar métodos: `findByProjectId`, `findByUserId`, `findByProjectIdAndUserId`

3. **Refactor Phase**:
   - Agregar índices compuestos
   - Implementar método `hasRole(projectId, userId, roleType)`

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Todos los tests de integración pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Constraint de unicidad funciona

**Related Requirements**: 3.1, 3.2, 3.3, 3.4, 3.5 (Role Assignment)
**Related Properties**: Property 11, 12, 13, 14


### Task 6.2: Implement RoleService

**Descripción**: Implementar lógica de negocio para asignación y gestión de roles.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test asignar rol EDITOR a usuario válido
     - Test asignar rol CONSULTANT a usuario válido
     - Test asignar rol a usuario no miembro del workspace lanza excepción
     - Test remover rol existente
     - Test remover rol inexistente no lanza excepción (idempotente)
     - Test hasRole retorna true para rol existente
     - Test hasRole retorna false para rol inexistente
     - Test getUserRoles retorna todos los roles del usuario en proyecto
   - Property Tests:
     - **Property 11**: *For any* usuario asignado rol, hasRole debe retornar true (**Validates: Property 11**)
     - **Property 12**: *For any* usuario con rol, remover rol debe hacer hasRole retornar false (**Validates: Property 12**)
     - **Property 13**: *For any* usuario, roles en proyecto A no afectan hasRole en proyecto B (**Validates: Property 13**)
   
2. **Green Phase - Implement Minimum Code**:
   - Crear `RoleService` con métodos de gestión de roles
   - Implementar `assignRole` con validación de membresía en workspace
   - Implementar `removeRole`
   - Implementar `hasRole` y `getUserRoles`
   - Integrar con WorkspaceService para validar membresía

3. **Refactor Phase**:
   - Agregar caché para consultas frecuentes de roles
   - Mejorar validaciones
   - Agregar transacciones

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Validación de membresía funciona
- [ ] Operaciones son idempotentes donde corresponde

**Related Requirements**: 3.1, 3.2, 3.3, 3.4, 3.5 (Role Assignment)
**Related Properties**: Property 11, 12, 13, 14

### Task 6.3: Implement RoleController

**Descripción**: Crear endpoints REST para gestión de roles.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Integration Tests:
     - Test POST /api/projects/{id}/roles asigna rol y retorna 200
     - Test POST /api/projects/{id}/roles a no-miembro retorna 400
     - Test DELETE /api/projects/{id}/roles/{userId}/{roleType} remueve rol
     - Test GET /api/projects/{id}/roles retorna lista de roles
     - Test operaciones sin ser author retornan 403
   - Contract Tests:
     - Test que response de lista de roles cumple contrato
   
2. **Green Phase - Implement Minimum Code**:
   - Crear `RoleController` con endpoints
   - Implementar DTOs: `AssignRoleRequest`, `ProjectRoleDTO`
   - Implementar mapeo de entidades a DTOs
   - Configurar autorización (solo author puede asignar roles)

3. **Refactor Phase**:
   - Extraer mapeo a `RoleMapper`
   - Mejorar mensajes de error

**Acceptance Criteria**:
- [ ] Todos los tests de integración pasan
- [ ] Contract tests pasan
- [ ] Autorización funciona correctamente

**Related Requirements**: 3.1, 3.2, 3.3, 3.5 (Role Assignment)

### Task 6.4: Implement EditorCorrection Entity and Repository

**Descripción**: Crear entidad para registrar correcciones realizadas por editores.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test que EditorCorrection tiene todos los campos requeridos
   - Integration Tests:
     - Test guardar EditorCorrection en BD
     - Test buscar correcciones por projectId
     - Test buscar correcciones por editorId
   - Property Tests:
     - **Property 22**: *For any* corrección de editor, debe registrarse editorId, timestamp, y contenido before/after (**Validates: Property 22**)
   
2. **Green Phase - Implement Minimum Code**:
   - Crear entidad `EditorCorrection` con anotaciones JPA
   - Crear `EditorCorrectionRepository`
   - Agregar métodos: `findByProjectId`, `findByEditorId`

3. **Refactor Phase**:
   - Agregar índices en projectId y editorId
   - Agregar método para obtener correcciones en rango de fechas

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Todos los tests de integración pasan
- [ ] Property tests pasan (100+ iteraciones)

**Related Requirements**: 6.2, 6.4 (Editor Corrections)
**Related Properties**: Property 22

### Task 6.5: Implement Editor Content Modification with Correction Tracking

**Descripción**: Extender ProjectService para permitir modificaciones de editores con tracking de correcciones.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test editor puede modificar contenido en estado EDITING
     - Test editor no puede modificar en estado CREATION
     - Test editor no puede modificar en estado CONSULTATION
     - Test editor no puede modificar en estado PUBLISHED
     - Test no-editor no puede modificar en estado EDITING
     - Test corrección se registra con metadata correcta
   - Property Tests:
     - **Property 21**: *For any* proyecto con editores, editores pueden modificar solo en EDITING; otros estados deben fallar (**Validates: Property 21**)
     - **Property 15**: *For any* contenido, guardar y recuperar debe retornar contenido idéntico (round-trip) (**Validates: Property 15**)
   
2. **Green Phase - Implement Minimum Code**:
   - Extender `ProjectService.updateContent` para validar rol EDITOR
   - Implementar validación de estado (solo EDITING permite ediciones)
   - Registrar EditorCorrection en cada modificación
   - Almacenar contentBefore y contentAfter

3. **Refactor Phase**:
   - Extraer validación de permisos a método privado
   - Mejorar cálculo de diff entre versiones
   - Agregar transacciones

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Solo editores pueden modificar en EDITING
- [ ] Correcciones se registran correctamente

**Related Requirements**: 6.1, 6.2, 6.3, 6.4 (Editor Corrections)
**Related Properties**: Property 15, 21, 22

---

## Milestone 7: Project Service - Change Proposals

### Task 7.1: Implement ChangeProposal Entity and Repository

**Descripción**: Crear entidad ChangeProposal para propuestas de cambio de consultants.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test que ChangeProposal tiene todos los campos requeridos
     - Test que status inicial es DRAFT
   - Integration Tests:
     - Test guardar ChangeProposal en BD
     - Test buscar propuestas por projectId
     - Test buscar propuestas por authorId
     - Test buscar propuestas por status
   - Property Tests:
     - **Property 25**: *For any* consultant creando propuesta con sección, propuesta debe linkear a sección y consultant (**Validates: Property 25**)
   
2. **Green Phase - Implement Minimum Code**:
   - Crear enum `ProposalStatus` (DRAFT, SUBMITTED, APPROVED, REJECTED, CANCELLED, MERGED)
   - Crear entidad `ChangeProposal` con anotaciones JPA
   - Crear `ChangeProposalRepository`
   - Agregar métodos: `findByProjectId`, `findByAuthorId`, `findByStatus`

3. **Refactor Phase**:
   - Agregar índices en projectId, authorId, status
   - Agregar método para buscar propuestas pendientes de aprobación

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Todos los tests de integración pasan
- [ ] Property tests pasan (100+ iteraciones)

**Related Requirements**: 7.1, 7.2, 7.4 (Change Proposal Creation)
**Related Properties**: Property 25


### Task 7.2: Implement Comment Entity and Repository

**Descripción**: Crear entidad Comment para discusiones en propuestas.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test que Comment tiene proposalId, authorId, content
   - Integration Tests:
     - Test guardar Comment en BD
     - Test buscar comentarios por proposalId en orden cronológico
   - Property Tests:
     - **Property 27**: *For any* comentario agregado, debe almacenarse con consultantId y timestamp (**Validates: Property 27**)
     - **Property 29**: *For any* propuesta con múltiples comentarios, query debe retornar comentarios en orden cronológico (**Validates: Property 29**)
   
2. **Green Phase - Implement Minimum Code**:
   - Crear entidad `Comment` con anotaciones JPA
   - Crear `CommentRepository`
   - Agregar método `findByProposalIdOrderByCreatedAtAsc`

3. **Refactor Phase**:
   - Agregar índice en proposalId
   - Agregar paginación para comentarios

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Todos los tests de integración pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Orden cronológico se mantiene

**Related Requirements**: 8.1, 8.3 (Change Proposal Discussion)
**Related Properties**: Property 27, 29

### Task 7.3: Implement ProposalService

**Descripción**: Implementar lógica de negocio para creación y gestión de propuestas.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test crear propuesta en estado CONSULTATION válido
     - Test crear propuesta en estado CREATION lanza excepción
     - Test crear propuesta en estado EDITING lanza excepción
     - Test crear propuesta en estado PUBLISHED lanza excepción
     - Test crear propuesta sin rol CONSULTANT lanza excepción
     - Test propuesta se inicializa con discussion vacía
     - Test submitProposal cambia status a SUBMITTED
     - Test submitProposal por no-author lanza excepción
     - Test submitProposal hace propuesta inmutable
     - Test cancelProposal cambia status a CANCELLED
     - Test cancelProposal por no-author lanza excepción
     - Test cancelProposal preserva datos
   - Property Tests:
     - **Property 24**: *For any* proyecto con consultants, crear propuesta solo válido en CONSULTATION (**Validates: Property 24**)
     - **Property 25**: *For any* consultant creando propuesta, propuesta debe linkear a sección y consultant (**Validates: Property 25**)
     - **Property 26**: *For any* propuesta nueva, discussion debe estar vacía (**Validates: Property 26**)
     - **Property 31**: *For any* author submitting propuesta, status debe cambiar a SUBMITTED y contenido almacenado (**Validates: Property 31**)
     - **Property 32**: *For any* propuesta SUBMITTED, intentos de modificar deben fallar (**Validates: Property 32**)
     - **Property 33**: *For any* propuesta, solo author puede submit (**Validates: Property 33**)
     - **Property 35**: *For any* author cancelando propuesta, status debe cambiar a CANCELLED (**Validates: Property 35**)
     - **Property 36**: *For any* propuesta cancelada, datos y discussion deben permanecer accesibles (**Validates: Property 36**)
     - **Property 37**: *For any* propuesta, solo author puede cancelar (**Validates: Property 37**)
     - **Property 38**: *For any* propuesta cancelada, no debe aparecer en lista de pendientes (**Validates: Property 38**)
   
2. **Green Phase - Implement Minimum Code**:
   - Crear `ProposalService` con métodos CRUD
   - Implementar `createProposal` con validación de estado y rol
   - Implementar `submitProposal` con validación de author
   - Implementar `cancelProposal` con validación de author
   - Implementar `validateProposalAuthor`

3. **Refactor Phase**:
   - Extraer validaciones a métodos privados
   - Agregar transacciones
   - Mejorar mensajes de error

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Validaciones de estado funcionan
- [ ] Validaciones de autor funcionan

**Related Requirements**: 7.1, 7.2, 7.3, 7.4, 7.5, 9.1, 9.2, 9.3, 10.1, 10.2, 10.3, 10.4 (Change Proposals)
**Related Properties**: Property 24, 25, 26, 31, 32, 33, 35, 36, 37, 38

### Task 7.4: Implement DiscussionService

**Descripción**: Implementar lógica de negocio para discusiones en propuestas.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test agregar comentario con rol CONSULTANT válido
     - Test agregar comentario sin rol CONSULTANT lanza excepción
     - Test getComments retorna comentarios en orden cronológico
     - Test proposal author puede ver todos los comentarios
   - Property Tests:
     - **Property 27**: *For any* comentario agregado, debe almacenarse con consultantId y timestamp (**Validates: Property 27**)
     - **Property 28**: *For any* propuesta, solo CONSULTANT puede agregar comentarios (**Validates: Property 28**)
     - **Property 29**: *For any* propuesta con comentarios, query debe retornar en orden cronológico (**Validates: Property 29**)
     - **Property 30**: *For any* propuesta, author puede ver todos los comentarios (**Validates: Property 30**)
   
2. **Green Phase - Implement Minimum Code**:
   - Crear `DiscussionService` con métodos de gestión de comentarios
   - Implementar `addComment` con validación de rol CONSULTANT
   - Implementar `getComments` con orden cronológico
   - Implementar `validateConsultantAccess`

3. **Refactor Phase**:
   - Agregar paginación para comentarios
   - Agregar caché para comentarios frecuentes
   - Mejorar validaciones

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Solo consultants pueden comentar
- [ ] Orden cronológico se mantiene

**Related Requirements**: 8.1, 8.2, 8.3, 8.4 (Change Proposal Discussion)
**Related Properties**: Property 27, 28, 29, 30

### Task 7.5: Implement ProposalController and DiscussionController

**Descripción**: Crear endpoints REST para propuestas y discusiones.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Integration Tests:
     - Test POST /api/projects/{id}/proposals crea propuesta en CONSULTATION
     - Test POST /api/projects/{id}/proposals en EDITING retorna 409
     - Test GET /api/projects/{id}/proposals retorna lista de propuestas
     - Test PUT /api/proposals/{id}/submit submite propuesta
     - Test PUT /api/proposals/{id}/cancel cancela propuesta
     - Test POST /api/proposals/{id}/discussions agrega comentario
     - Test GET /api/proposals/{id}/discussions retorna comentarios
     - Test operaciones sin rol apropiado retornan 403
   - Contract Tests:
     - Test que response de crear propuesta cumple contrato
     - Test que lista de comentarios cumple contrato
   
2. **Green Phase - Implement Minimum Code**:
   - Crear `ProposalController` con endpoints
   - Crear `DiscussionController` con endpoints
   - Implementar DTOs: `CreateProposalRequest`, `ProposalDTO`, `SubmitProposalRequest`, `AddCommentRequest`, `CommentDTO`
   - Implementar mapeo de entidades a DTOs
   - Configurar autorización

3. **Refactor Phase**:
   - Extraer mapeo a `ProposalMapper` y `CommentMapper`
   - Agregar paginación
   - Mejorar documentación de API

**Acceptance Criteria**:
- [ ] Todos los tests de integración pasan
- [ ] Contract tests pasan
- [ ] Autorización funciona correctamente

**Related Requirements**: 7.1, 7.2, 7.5, 8.1, 8.2, 9.1, 9.3, 10.1, 10.3 (Proposals and Discussions)

---

## Milestone 8: Project Service - Discussions & Merging

### Task 8.1: Implement MergeRequest Entity and Repository

**Descripción**: Crear entidad MergeRequest para solicitudes de fusión de propuestas.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test que MergeRequest tiene sourceProposalId, targetProposalId, requesterId
     - Test que status inicial es PENDING
   - Integration Tests:
     - Test guardar MergeRequest en BD
     - Test buscar merge requests por sourceProposalId
     - Test buscar merge requests por targetProposalId
     - Test buscar merge requests pendientes
   - Property Tests:
     - **Property 39**: *For any* proposal author iniciando merge, debe crearse merge request (**Validates: Property 39**)
   
2. **Green Phase - Implement Minimum Code**:
   - Crear enum `MergeStatus` (PENDING, APPROVED, REJECTED)
   - Crear entidad `MergeRequest` con anotaciones JPA
   - Crear `MergeRequestRepository`
   - Agregar métodos: `findBySourceProposalId`, `findByTargetProposalId`, `findByStatus`

3. **Refactor Phase**:
   - Agregar índices en sourceProposalId, targetProposalId, status
   - Agregar método para buscar merge requests pendientes de un usuario

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Todos los tests de integración pasan
- [ ] Property tests pasan (100+ iteraciones)

**Related Requirements**: 11.1 (Change Proposal Merging)
**Related Properties**: Property 39


### Task 8.2: Implement Proposal Merging Logic in ProposalService

**Descripción**: Extender ProposalService para soportar fusión de propuestas.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test requestMerge crea MergeRequest con status PENDING
     - Test requestMerge por no-author lanza excepción
     - Test approveMerge combina propuestas
     - Test approveMerge combina discussions en orden cronológico
     - Test approveMerge asigna co-autores
     - Test approveMerge cambia status source a MERGED
     - Test approveMerge por no-target-author lanza excepción
     - Test rejectMerge mantiene propuestas separadas
     - Test rejectMerge cambia status merge request a REJECTED
   - Property Tests:
     - **Property 39**: *For any* proposal author iniciando merge, debe crearse merge request (**Validates: Property 39**)
     - **Property 40**: *For any* merge request aprobado, propuestas deben combinarse (**Validates: Property 40**)
     - **Property 41**: *For any* dos propuestas mergeadas, discussion resultante debe contener ambas en orden cronológico (**Validates: Property 41**)
     - **Property 42**: *For any* propuesta mergeada, ambos authors originales deben ser co-autores (**Validates: Property 42**)
     - **Property 43**: *For any* merge request rechazado, propuestas deben permanecer separadas (**Validates: Property 43**)
   
2. **Green Phase - Implement Minimum Code**:
   - Implementar `requestMerge` en ProposalService
   - Implementar `approveMerge` que:
     - Combina contenido de propuestas
     - Combina discussions en orden cronológico
     - Asigna co-autores
     - Marca source proposal como MERGED
   - Implementar `rejectMerge`
   - Agregar validaciones de autor

3. **Refactor Phase**:
   - Extraer lógica de merge a clase `ProposalMerger`
   - Mejorar algoritmo de combinación de discussions
   - Agregar transacciones

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Merge combina propuestas correctamente
- [ ] Discussions se combinan en orden cronológico

**Related Requirements**: 11.1, 11.2, 11.3, 11.4, 11.5 (Change Proposal Merging)
**Related Properties**: Property 39, 40, 41, 42, 43

### Task 8.3: Implement Proposal Approval and Rejection in ProjectService

**Descripción**: Extender ProjectService para aprobar/rechazar propuestas.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test approveProposal cambia status a APPROVED
     - Test approveProposal incorpora cambios al documento
     - Test approveProposal registra timestamp
     - Test approveProposal por no-author lanza excepción
     - Test rejectProposal cambia status a REJECTED
     - Test rejectProposal preserva contenido original
     - Test rejectProposal registra rejection reason
     - Test rejectProposal por no-author lanza excepción
   - Property Tests:
     - **Property 44**: *For any* project author aprobando propuesta, status debe cambiar a APPROVED y cambios incorporados (**Validates: Property 44**)
     - **Property 45**: *For any* project author rechazando propuesta, status debe cambiar a REJECTED, contenido preservado, y reason registrado (**Validates: Property 45**)
     - **Property 46**: *For any* propuesta, solo project author puede aprobar/rechazar (**Validates: Property 46**)
   
2. **Green Phase - Implement Minimum Code**:
   - Implementar `approveProposal` en ProposalService
   - Implementar `rejectProposal` en ProposalService
   - Integrar con ProjectService para actualizar contenido
   - Validar que solo project author puede aprobar/rechazar

3. **Refactor Phase**:
   - Extraer lógica de aplicación de cambios a método privado
   - Mejorar tracking de cambios
   - Agregar transacciones

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Solo project author puede aprobar/rechazar
- [ ] Cambios se aplican correctamente

**Related Requirements**: 12.1, 12.2, 12.3, 12.4, 12.5 (Change Proposal Approval and Rejection)
**Related Properties**: Property 44, 45, 46

### Task 8.4: Extend ProposalController with Merge and Approval Endpoints

**Descripción**: Agregar endpoints REST para merge, aprobación y rechazo de propuestas.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Integration Tests:
     - Test POST /api/proposals/{id}/merge crea merge request
     - Test PUT /api/proposals/{id}/approve aprueba propuesta
     - Test PUT /api/proposals/{id}/reject rechaza propuesta
     - Test operaciones sin permisos retornan 403
   - Contract Tests:
     - Test que response de merge request cumple contrato
   
2. **Green Phase - Implement Minimum Code**:
   - Extender `ProposalController` con nuevos endpoints
   - Implementar DTOs: `MergeRequest`, `RejectProposalRequest`
   - Configurar autorización

3. **Refactor Phase**:
   - Mejorar documentación de API
   - Agregar validaciones adicionales

**Acceptance Criteria**:
- [ ] Todos los tests de integración pasan
- [ ] Contract tests pasan
- [ ] Autorización funciona correctamente

**Related Requirements**: 11.1, 12.1, 12.2 (Merging and Approval)

---

## Milestone 9: Project Service - Versioning & History

### Task 9.1: Implement DocumentVersion Entity and Repository

**Descripción**: Crear entidad DocumentVersion para versiones publicadas de documentos.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test que DocumentVersion tiene projectId, versionNumber, content
     - Test que combinación (projectId, versionNumber) debe ser única
   - Integration Tests:
     - Test guardar DocumentVersion en BD
     - Test buscar versiones por projectId en orden cronológico
     - Test buscar versión específica por projectId y versionNumber
     - Test intentar guardar versión duplicada lanza excepción
   - Property Tests:
     - **Property 48**: *For any* proyecto con múltiples publicaciones, version numbers deben ser secuenciales (1, 2, 3, ...) (**Validates: Property 48**)
     - **Property 49**: *For any* versión generada, intentos de modificar deben fallar (inmutabilidad) (**Validates: Property 49**)
   
2. **Green Phase - Implement Minimum Code**:
   - Crear entidad `DocumentVersion` con anotaciones JPA
   - Crear `DocumentVersionRepository`
   - Agregar constraint único en (projectId, versionNumber)
   - Agregar métodos: `findByProjectIdOrderByVersionNumberAsc`, `findByProjectIdAndVersionNumber`

3. **Refactor Phase**:
   - Agregar índice compuesto en (projectId, versionNumber)
   - Implementar método para obtener última versión

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Todos los tests de integración pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Constraint de unicidad funciona

**Related Requirements**: 13.1, 13.5, 13.6 (Document Version Generation)
**Related Properties**: Property 48, 49

### Task 9.2: Implement VersionCorrection and VersionProposal Entities

**Descripción**: Crear entidades para asociar correcciones y propuestas a versiones.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test que VersionCorrection tiene versionId, editorId, description
     - Test que VersionProposal tiene versionId, proposalId, finalStatus
   - Integration Tests:
     - Test guardar VersionCorrection en BD
     - Test guardar VersionProposal en BD
     - Test buscar correcciones por versionId
     - Test buscar propuestas por versionId
   
2. **Green Phase - Implement Minimum Code**:
   - Crear entidad `VersionCorrection` con anotaciones JPA
   - Crear entidad `VersionProposal` con anotaciones JPA
   - Crear `VersionCorrectionRepository`
   - Crear `VersionProposalRepository`
   - Agregar métodos: `findByVersionId`

3. **Refactor Phase**:
   - Agregar índices en versionId
   - Agregar métodos para filtrar por status

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Todos los tests de integración pasan

**Related Requirements**: 13.2, 13.3, 13.4 (Version History)


### Task 9.3: Implement VersionService with Version Generation

**Descripción**: Implementar lógica de negocio para generación de versiones al publicar.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test generateVersion crea DocumentVersion con versionNumber secuencial
     - Test generateVersion incluye todas las correcciones de editores
     - Test generateVersion incluye todas las propuestas aprobadas con discussions
     - Test generateVersion incluye todas las propuestas rechazadas con reasons
     - Test generateVersion se llama automáticamente en transición a PUBLISHED
     - Test getVersions retorna versiones en orden cronológico
     - Test getVersion retorna versión específica
     - Test compareVersions identifica diferencias entre versiones
   - Property Tests:
     - **Property 19**: *For any* proyecto transicionando a PUBLISHED, debe generarse nueva versión (**Validates: Property 19**)
     - **Property 47**: *For any* proyecto transicionando a PUBLISHED, versión debe incluir todas correcciones, propuestas aprobadas con discussions, y propuestas rechazadas con reasons (**Validates: Property 47**)
     - **Property 48**: *For any* proyecto con múltiples publicaciones, version numbers deben ser secuenciales (**Validates: Property 48**)
     - **Property 50**: *For any* proyecto con múltiples versiones, query debe retornar en orden cronológico (**Validates: Property 50**)
     - **Property 51**: *For any* versión publicada, recuperar debe retornar contenido exacto de publicación (**Validates: Property 51**)
     - **Property 52**: *For any* versión, ver detalles debe mostrar todas correcciones y propuestas con discussions (**Validates: Property 52**)
     - **Property 53**: *For any* dos versiones consecutivas, comparación debe identificar diferencias (**Validates: Property 53**)
   
2. **Green Phase - Implement Minimum Code**:
   - Crear `VersionService` con métodos de gestión de versiones
   - Implementar `generateVersion` que:
     - Calcula siguiente versionNumber
     - Crea DocumentVersion con contenido actual
     - Copia EditorCorrections a VersionCorrections
     - Copia ChangeProposals a VersionProposals
   - Integrar con ProjectService para llamar en transición a PUBLISHED
   - Implementar `getVersions`, `getVersion`, `compareVersions`

3. **Refactor Phase**:
   - Extraer lógica de copia de correcciones/propuestas a métodos privados
   - Implementar algoritmo de diff eficiente para compareVersions
   - Agregar transacciones

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Versiones se generan automáticamente al publicar
- [ ] Versiones incluyen todo el historial

**Related Requirements**: 5.3, 13.1, 13.2, 13.3, 13.4, 13.5, 14.1, 14.2, 14.3, 14.4, 14.5 (Version Generation and History)
**Related Properties**: Property 19, 47, 48, 50, 51, 52, 53

### Task 9.4: Implement Iterative Workflow Cycle Support

**Descripción**: Implementar soporte para múltiples ciclos de edición-consulta-publicación.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test transición PUBLISHED → EDITING inicia nuevo ciclo
     - Test nuevo ciclo preserva versiones anteriores
     - Test propuestas en nuevo ciclo son independientes de ciclos anteriores
     - Test propuestas tienen cycleNumber asociado
     - Test nueva versión incrementa versionNumber
   - Property Tests:
     - **Property 20**: *For any* proyecto en PUBLISHED, transicionar a EDITING debe iniciar nuevo ciclo (**Validates: Property 20**)
     - **Property 54**: *For any* proyecto iniciando nuevo ciclo, versiones anteriores deben permanecer accesibles (**Validates: Property 54**)
     - **Property 55**: *For any* proyecto con múltiples ciclos, propuestas en un ciclo no deben afectar otros ciclos (**Validates: Property 55**)
     - **Property 56**: *For any* propuesta, debe estar asociada con cycleNumber (**Validates: Property 56**)
   
2. **Green Phase - Implement Minimum Code**:
   - Agregar campo `cycleNumber` a Project entity
   - Incrementar cycleNumber en transición PUBLISHED → EDITING
   - Asociar ChangeProposals con cycleNumber al crear
   - Filtrar propuestas por cycleNumber en queries

3. **Refactor Phase**:
   - Agregar índice en (projectId, cycleNumber)
   - Implementar método para obtener propuestas del ciclo actual
   - Mejorar visualización de ciclos en API

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Ciclos funcionan correctamente
- [ ] Propuestas se aíslan por ciclo

**Related Requirements**: 15.1, 15.2, 15.3, 15.4, 15.5 (Iterative Workflow Cycles)
**Related Properties**: Property 20, 54, 55, 56

### Task 9.5: Implement VersionController

**Descripción**: Crear endpoints REST para consulta de versiones e historial.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Integration Tests:
     - Test GET /api/projects/{id}/versions retorna lista de versiones
     - Test GET /api/projects/{id}/versions/{versionNumber} retorna versión específica
     - Test GET /api/projects/{id}/versions/{v1}/compare/{v2} retorna diferencias
     - Test versiones retornan correcciones y propuestas completas
   - Contract Tests:
     - Test que response de versión cumple contrato
     - Test que response de comparación cumple contrato
   
2. **Green Phase - Implement Minimum Code**:
   - Crear `VersionController` con endpoints
   - Implementar DTOs: `VersionDTO`, `VersionDetailDTO`, `VersionComparisonDTO`
   - Implementar mapeo de entidades a DTOs
   - Incluir correcciones y propuestas en VersionDetailDTO

3. **Refactor Phase**:
   - Extraer mapeo a `VersionMapper`
   - Agregar paginación para lista de versiones
   - Optimizar queries para incluir relaciones

**Acceptance Criteria**:
- [ ] Todos los tests de integración pasan
- [ ] Contract tests pasan
- [ ] Versiones incluyen historial completo

**Related Requirements**: 14.1, 14.2, 14.3, 14.4, 14.5 (Version History)

---

## Milestone 10: Project Service - Index Management & Export

### Task 10.1: Implement IndexHistory Entity and Repository

**Descripción**: Crear entidad IndexHistory para tracking de cambios en el índice del proyecto.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test que IndexHistory tiene projectId, previousIndex, newIndex, modifiedBy
   - Integration Tests:
     - Test guardar IndexHistory en BD
     - Test buscar historial por projectId en orden cronológico
   - Property Tests:
     - **Property 73**: *For any* modificación de índice, debe almacenarse y crearse history entry (**Validates: Property 73**)
   
2. **Green Phase - Implement Minimum Code**:
   - Crear entidad `IndexHistory` con anotaciones JPA
   - Crear `IndexHistoryRepository`
   - Agregar método `findByProjectIdOrderByModifiedAtAsc`

3. **Refactor Phase**:
   - Agregar índice en projectId
   - Implementar método para obtener última modificación

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Todos los tests de integración pasan
- [ ] Property tests pasan (100+ iteraciones)

**Related Requirements**: 21.1, 21.4 (Index Management)
**Related Properties**: Property 73

### Task 10.2: Implement Index Modification in ProjectService

**Descripción**: Extender ProjectService para permitir modificación de índice con tracking.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test updateIndex en estado CREATION válido
     - Test updateIndex en estado EDITING válido
     - Test updateIndex en estado CONSULTATION válido
     - Test updateIndex en estado PUBLISHED válido
     - Test updateIndex por no-author lanza excepción
     - Test updateIndex registra IndexHistory
     - Test updateIndex con formato inválido lanza excepción
     - Test updateIndex preserva asociación contenido-sección
   - Property Tests:
     - **Property 73**: *For any* author modificando índice, debe almacenarse y crearse history entry (**Validates: Property 73**)
     - **Property 74**: *For any* proyecto en cualquier estado, author puede modificar índice (**Validates: Property 74**)
     - **Property 75**: *For any* proyecto con contenido asociado a secciones, reorganizar índice debe preservar asociaciones (**Validates: Property 75**)
     - **Property 76**: *For any* modificación de índice que rompa integridad, debe rechazarse (**Validates: Property 76**)
   
2. **Green Phase - Implement Minimum Code**:
   - Implementar `updateIndex` en ProjectService
   - Validar que solo author puede modificar
   - Registrar IndexHistory en cada modificación
   - Implementar validación de formato de índice
   - Preservar asociaciones contenido-sección

3. **Refactor Phase**:
   - Extraer validación de formato a clase `IndexValidator`
   - Mejorar algoritmo de preservación de asociaciones
   - Agregar transacciones

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Índice se puede modificar en cualquier estado
- [ ] Historial se registra correctamente

**Related Requirements**: 21.1, 21.2, 21.3, 21.4, 21.5 (Index Management)
**Related Properties**: Property 73, 74, 75, 76


### Task 10.3: Implement ProjectCoverImage Entity and Repository

**Descripción**: Crear entidad ProjectCoverImage para gestión de imágenes de portada.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test que ProjectCoverImage tiene projectId, fileName, contentType, storageKey
     - Test que projectId debe ser único (un proyecto = una imagen)
   - Integration Tests:
     - Test guardar ProjectCoverImage en BD
     - Test buscar imagen por projectId
     - Test actualizar imagen existente
     - Test eliminar imagen
   - Property Tests:
     - **Property 82**: *For any* author subiendo imagen, debe almacenarse y asociarse con proyecto (**Validates: Property 82**)
     - **Property 85**: *For any* proyecto con imagen, author puede actualizar o remover (round-trip: upload→verify→remove→verify absence) (**Validates: Property 85**)
   
2. **Green Phase - Implement Minimum Code**:
   - Crear entidad `ProjectCoverImage` con anotaciones JPA
   - Crear `ProjectCoverImageRepository`
   - Agregar constraint único en projectId
   - Agregar métodos: `findByProjectId`, `deleteByProjectId`

3. **Refactor Phase**:
   - Agregar índice en projectId
   - Implementar método para verificar existencia

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Todos los tests de integración pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Constraint de unicidad funciona

**Related Requirements**: 23.1, 23.4 (Cover Image)
**Related Properties**: Property 82, 85

### Task 10.4: Implement Cover Image Upload and Management

**Descripción**: Implementar lógica de negocio para subir, validar y gestionar imágenes de portada.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test uploadCoverImage con PNG válido
     - Test uploadCoverImage con JPG válido
     - Test uploadCoverImage con WebP válido
     - Test uploadCoverImage con formato inválido lanza excepción
     - Test uploadCoverImage con tamaño > 5MB lanza excepción
     - Test uploadCoverImage por no-author lanza excepción
     - Test removeCoverImage elimina imagen
     - Test removeCoverImage por no-author lanza excepción
     - Test proyecto sin imagen retorna placeholder
   - Property Tests:
     - **Property 82**: *For any* author subiendo imagen válida, debe almacenarse y asociarse (**Validates: Property 82**)
     - **Property 83**: *For any* imagen, solo PNG/JPG/WebP deben aceptarse (**Validates: Property 83**)
     - **Property 84**: *For any* imagen > 5MB, debe rechazarse (**Validates: Property 84**)
     - **Property 85**: *For any* proyecto con imagen, author puede actualizar/remover (**Validates: Property 85**)
     - **Property 86**: *For any* proyecto sin imagen, debe retornar placeholder (**Validates: Property 86**)
   
2. **Green Phase - Implement Minimum Code**:
   - Implementar `uploadCoverImage` en ProjectService
   - Validar formato (PNG, JPG, WebP)
   - Validar tamaño (max 5MB)
   - Almacenar archivo en sistema de archivos o S3
   - Implementar `removeCoverImage`
   - Implementar lógica de placeholder

3. **Refactor Phase**:
   - Extraer validación a clase `ImageValidator`
   - Implementar servicio de almacenamiento abstracto (FileStorage interface)
   - Agregar compresión de imágenes
   - Mejorar manejo de errores

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Validaciones de formato y tamaño funcionan
- [ ] Solo author puede gestionar imagen

**Related Requirements**: 23.1, 23.2, 23.3, 23.4, 23.5 (Cover Image)
**Related Properties**: Property 82, 83, 84, 85, 86

### Task 10.5: Implement ShareableLink Entity and LinkAccessLog

**Descripción**: Crear entidades para links compartibles y tracking de accesos.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test que ShareableLink tiene versionId, token, createdBy, allowDownload
     - Test que token debe ser único
   - Integration Tests:
     - Test guardar ShareableLink en BD
     - Test buscar link por token
     - Test buscar links por versionId
     - Test guardar LinkAccessLog en BD
     - Test buscar access logs por linkId
   - Property Tests:
     - **Property 78**: *For any* conjunto de links generados, todos los tokens deben ser únicos (**Validates: Property 78**)
   
2. **Green Phase - Implement Minimum Code**:
   - Crear entidad `ShareableLink` con anotaciones JPA
   - Crear entidad `LinkAccessLog` con anotaciones JPA
   - Crear `ShareableLinkRepository`
   - Crear `LinkAccessLogRepository`
   - Agregar constraint único en token
   - Agregar métodos: `findByToken`, `findByVersionId`

3. **Refactor Phase**:
   - Agregar índices en token y versionId
   - Implementar método para buscar links activos

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Todos los tests de integración pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Constraint de unicidad en token funciona

**Related Requirements**: 22.2, 22.5 (Shareable Links)
**Related Properties**: Property 78

### Task 10.6: Implement PDF Export and Shareable Link Generation

**Descripción**: Implementar exportación a PDF y generación de links compartibles.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test exportToPDF genera PDF con formato preservado
     - Test generateShareableLink crea link con token único
     - Test generateShareableLink por no-author lanza excepción
     - Test getVersionByShareableToken retorna versión sin autenticación
     - Test updateLinkPermissions actualiza allowDownload
     - Test updateLinkPermissions por no-author lanza excepción
     - Test revokeShareableLink desactiva link
     - Test trackLinkAccess registra VIEW
     - Test trackLinkAccess registra DOWNLOAD
   - Property Tests:
     - **Property 77**: *For any* versión exportada a PDF, formato debe preservarse (**Validates: Property 77**)
     - **Property 78**: *For any* conjunto de links, tokens deben ser únicos (**Validates: Property 78**)
     - **Property 79**: *For any* link, solo project author puede modificar permisos o revocar (**Validates: Property 79**)
     - **Property 80**: *For any* link activo, acceso debe mostrar contenido sin autenticación (**Validates: Property 80**)
     - **Property 81**: *For any* acceso a link, debe registrarse log entry con timestamp (**Validates: Property 81**)
   
2. **Green Phase - Implement Minimum Code**:
   - Implementar `exportToPDF` en VersionService usando biblioteca PDF (iText o similar)
   - Implementar `generateShareableLink` con generación de token UUID
   - Implementar `getVersionByShareableToken` sin requerir autenticación
   - Implementar `updateLinkPermissions` con validación de author
   - Implementar `revokeShareableLink`
   - Implementar `trackLinkAccess` para registrar accesos

3. **Refactor Phase**:
   - Extraer generación de PDF a clase `PdfGenerator`
   - Mejorar formato de PDF con estilos
   - Implementar caché para PDFs generados
   - Agregar expiración automática de links

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] PDF se genera con formato correcto
- [ ] Links funcionan sin autenticación
- [ ] Tracking de accesos funciona

**Related Requirements**: 22.1, 22.2, 22.3, 22.4, 22.5 (Export and Sharing)
**Related Properties**: Property 77, 78, 79, 80, 81

### Task 10.7: Extend VersionController with Export and Sharing Endpoints

**Descripción**: Agregar endpoints REST para exportación y gestión de links compartibles.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Integration Tests:
     - Test POST /api/projects/{id}/versions/{v}/export/pdf retorna PDF
     - Test POST /api/projects/{id}/versions/{v}/shareable-link genera link
     - Test GET /api/versions/shared/{token} retorna versión sin autenticación
     - Test GET /api/projects/{id}/versions/{v}/shareable-links retorna lista
     - Test PUT /api/versions/shareable-links/{linkId} actualiza permisos
     - Test DELETE /api/versions/shareable-links/{linkId} revoca link
     - Test operaciones sin ser author retornan 403
   - Contract Tests:
     - Test que response de shareable link cumple contrato
   
2. **Green Phase - Implement Minimum Code**:
   - Extender `VersionController` con nuevos endpoints
   - Implementar DTOs: `ShareableLinkDTO`, `ShareableLinkRequest`, `UpdateLinkPermissionsRequest`
   - Configurar endpoint público para /shared/{token}
   - Configurar autorización para otros endpoints

3. **Refactor Phase**:
   - Agregar Content-Disposition header para descarga de PDF
   - Mejorar documentación de API
   - Agregar rate limiting para endpoints públicos

**Acceptance Criteria**:
- [ ] Todos los tests de integración pasan
- [ ] Contract tests pasan
- [ ] Endpoint público funciona sin autenticación
- [ ] Autorización funciona en endpoints protegidos

**Related Requirements**: 22.1, 22.2, 22.3, 22.4 (Export and Sharing)

### Task 10.8: Extend ProjectController with Index and Cover Image Endpoints

**Descripción**: Agregar endpoints REST para gestión de índice e imagen de portada.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Integration Tests:
     - Test PUT /api/projects/{id}/index actualiza índice
     - Test GET /api/projects/{id}/index-history retorna historial
     - Test POST /api/projects/{id}/cover-image sube imagen
     - Test DELETE /api/projects/{id}/cover-image elimina imagen
     - Test operaciones sin ser author retornan 403
   - Contract Tests:
     - Test que response de index history cumple contrato
     - Test que response de cover image cumple contrato
   
2. **Green Phase - Implement Minimum Code**:
   - Extender `ProjectController` con nuevos endpoints
   - Implementar DTOs: `UpdateIndexRequest`, `IndexHistoryDTO`, `ProjectCoverImageDTO`
   - Configurar multipart para upload de imagen
   - Configurar autorización

3. **Refactor Phase**:
   - Agregar validación de tamaño de archivo en controller
   - Mejorar documentación de API

**Acceptance Criteria**:
- [ ] Todos los tests de integración pasan
- [ ] Contract tests pasan
- [ ] Upload de imagen funciona correctamente
- [ ] Autorización funciona

**Related Requirements**: 21.1, 21.4, 23.1, 23.4 (Index and Cover Image)

---

## Milestone 11: Notifications & Authorization

### Task 11.1: Implement Notification Entity and Repository

**Descripción**: Crear entidad Notification para sistema de notificaciones.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test que Notification tiene userId, type, message, relatedEntityId
   - Integration Tests:
     - Test guardar Notification en BD
     - Test buscar notificaciones por userId
     - Test buscar notificaciones no leídas
     - Test marcar notificación como leída
   - Property Tests:
     - **Property 67**: *For any* notificación creada, debe permanecer almacenada hasta marcarse como leída (**Validates: Property 67**)
   
2. **Green Phase - Implement Minimum Code**:
   - Crear enum `NotificationType` (STATE_TRANSITION, PROPOSAL_SUBMITTED, PROPOSAL_APPROVED, PROPOSAL_REJECTED, MERGE_REQUEST, ROLE_ASSIGNED)
   - Crear entidad `Notification` con anotaciones JPA
   - Crear `NotificationRepository`
   - Agregar métodos: `findByUserId`, `findByUserIdAndReadFalse`

3. **Refactor Phase**:
   - Agregar índices en userId y read
   - Implementar paginación para notificaciones

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Todos los tests de integración pasan
- [ ] Property tests pasan (100+ iteraciones)

**Related Requirements**: 19.1, 19.2, 19.3, 19.4, 19.5 (Notification System)
**Related Properties**: Property 67


### Task 11.2: Implement NotificationService

**Descripción**: Implementar lógica de negocio para creación y gestión de notificaciones.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test notifyStateTransition crea notificaciones para usuarios con roles
     - Test notifyProposalSubmitted crea notificación para project author
     - Test notifyProposalResolution crea notificación para proposal author
     - Test notifyMergeRequest crea notificación para target author
     - Test markAsRead marca notificación como leída
     - Test getUserNotifications retorna notificaciones del usuario
   - Property Tests:
     - **Property 64**: *For any* transición de estado, deben crearse notificaciones para todos los usuarios con roles (**Validates: Property 64**)
     - **Property 65**: *For any* aprobación/rechazo de propuesta, debe crearse notificación para proposal author (**Validates: Property 65**)
     - **Property 66**: *For any* merge request, debe crearse notificación para target author (**Validates: Property 66**)
     - **Property 67**: *For any* notificación creada, debe permanecer hasta marcarse como leída (**Validates: Property 67**)
   
2. **Green Phase - Implement Minimum Code**:
   - Crear `NotificationService` con métodos de notificación
   - Implementar `notifyStateTransition`
   - Implementar `notifyProposalSubmitted`
   - Implementar `notifyProposalResolution`
   - Implementar `notifyMergeRequest`
   - Implementar `markAsRead` y `getUserNotifications`
   - Integrar con servicios existentes para disparar notificaciones

3. **Refactor Phase**:
   - Implementar notificaciones asíncronas con `@Async`
   - Agregar templates para mensajes de notificación
   - Implementar batching de notificaciones

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Notificaciones se crean en eventos correctos
- [ ] Notificaciones persisten hasta leerse

**Related Requirements**: 19.1, 19.2, 19.3, 19.4, 19.5 (Notification System)
**Related Properties**: Property 64, 65, 66, 67

### Task 11.3: Implement NotificationController

**Descripción**: Crear endpoints REST para consulta y gestión de notificaciones.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Integration Tests:
     - Test GET /api/notifications retorna notificaciones del usuario autenticado
     - Test GET /api/notifications/unread retorna solo no leídas
     - Test PUT /api/notifications/{id}/read marca como leída
     - Test operaciones sin autenticación retornan 401
   - Contract Tests:
     - Test que response de notificaciones cumple contrato
   
2. **Green Phase - Implement Minimum Code**:
   - Crear `NotificationController` con endpoints
   - Implementar DTOs: `NotificationDTO`
   - Implementar mapeo de entidades a DTOs
   - Configurar autorización

3. **Refactor Phase**:
   - Agregar paginación
   - Implementar WebSocket para notificaciones en tiempo real
   - Mejorar documentación de API

**Acceptance Criteria**:
- [ ] Todos los tests de integración pasan
- [ ] Contract tests pasan
- [ ] Paginación funciona correctamente

**Related Requirements**: 19.1, 19.2, 19.3, 19.4, 19.5 (Notification System)

### Task 11.4: Implement Comprehensive Authorization Checks

**Descripción**: Implementar y verificar todas las validaciones de autorización en servicios.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test usuario sin rol EDITOR no puede modificar contenido en EDITING
     - Test usuario sin rol CONSULTANT no puede crear propuestas en CONSULTATION
     - Test usuario sin rol CONSULTANT no puede comentar en propuestas
     - Test no-author no puede transicionar estados
     - Test no-author no puede aprobar/rechazar propuestas
     - Test no-author no puede asignar roles
     - Test no-author no puede modificar índice
     - Test no-author no puede gestionar cover image
   - Property Tests:
     - **Property 57**: *For any* usuario intentando acción con rol requerido, debe suceder si tiene rol y fallar si no (**Validates: Property 57**)
     - **Property 58**: *For any* proyecto, solo author puede transicionar estados (**Validates: Property 58**)
   
2. **Green Phase - Implement Minimum Code**:
   - Revisar todos los servicios y agregar validaciones faltantes
   - Implementar método de utilidad `validateUserHasRole`
   - Implementar método de utilidad `validateProjectAuthor`
   - Lanzar `AuthorizationException` en violaciones

3. **Refactor Phase**:
   - Extraer validaciones a clase `AuthorizationValidator`
   - Implementar anotaciones custom para autorización
   - Mejorar mensajes de error

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Todas las operaciones validan autorización
- [ ] Mensajes de error son claros

**Related Requirements**: 16.1, 16.2, 16.3, 16.4, 16.5, 16.6 (Authentication and Authorization)
**Related Properties**: Property 57, 58

---

## Milestone 12: Search, Filtering & Concurrency

### Task 12.1: Implement Search and Filtering in Repositories

**Descripción**: Agregar métodos de búsqueda y filtrado en repositories.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test buscar proyectos por nombre (case-insensitive)
     - Test filtrar proyectos por estado
     - Test filtrar proyectos por workspace
     - Test filtrar propuestas por status
     - Test buscar propuestas por contenido
     - Test buscar propuestas por author
   - Integration Tests:
     - Test búsquedas retornan resultados correctos
     - Test búsquedas respetan permisos de acceso
   - Property Tests:
     - **Property 68**: *For any* búsqueda por nombre, resultados deben incluir proyectos con acceso y excluir sin acceso (**Validates: Property 68**)
     - **Property 69**: *For any* filtro por estado, todos los resultados deben estar en ese estado (**Validates: Property 69**)
     - **Property 70**: *For any* filtro por status de propuesta, todos los resultados deben tener ese status (**Validates: Property 70**)
     - **Property 71**: *For any* filtro por workspace, todos los resultados deben pertenecer a ese workspace (**Validates: Property 71**)
     - **Property 72**: *For any* búsqueda de propuestas, resultados deben coincidir con criterio (**Validates: Property 72**)
   
2. **Green Phase - Implement Minimum Code**:
   - Agregar métodos en `ProjectRepository`:
     - `findByNameContainingIgnoreCase`
     - `findByState`
     - `findByWorkspaceId`
   - Agregar métodos en `ChangeProposalRepository`:
     - `findByStatus`
     - `findByProposedContentContaining`
     - `findByAuthorId`
   - Implementar filtrado con acceso en servicios

3. **Refactor Phase**:
   - Implementar Specifications para queries complejas
   - Agregar índices para mejorar performance de búsquedas
   - Implementar paginación

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Todos los tests de integración pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Búsquedas respetan permisos

**Related Requirements**: 20.1, 20.2, 20.3, 20.4, 20.5 (Search and Filtering)
**Related Properties**: Property 68, 69, 70, 71, 72

### Task 12.2: Implement Search and Filter Endpoints

**Descripción**: Crear endpoints REST para búsqueda y filtrado.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Integration Tests:
     - Test GET /api/projects/search?name={name} retorna proyectos coincidentes
     - Test GET /api/projects?state={state} filtra por estado
     - Test GET /api/projects?workspaceId={id} filtra por workspace
     - Test GET /api/proposals?status={status} filtra por status
     - Test GET /api/proposals/search?query={query} busca en contenido
     - Test búsquedas respetan permisos de usuario
   - Contract Tests:
     - Test que responses de búsqueda cumplen contrato
   
2. **Green Phase - Implement Minimum Code**:
   - Extender `ProjectController` con endpoints de búsqueda
   - Extender `ProposalController` con endpoints de búsqueda
   - Implementar query parameters para filtros
   - Configurar paginación

3. **Refactor Phase**:
   - Implementar búsqueda full-text con Elasticsearch (opcional)
   - Agregar ordenamiento configurable
   - Mejorar documentación de API

**Acceptance Criteria**:
- [ ] Todos los tests de integración pasan
- [ ] Contract tests pasan
- [ ] Paginación funciona correctamente
- [ ] Permisos se respetan

**Related Requirements**: 20.1, 20.2, 20.3, 20.4, 20.5 (Search and Filtering)

### Task 12.3: Implement Optimistic Locking and Conflict Detection

**Descripción**: Verificar y mejorar manejo de concurrencia con optimistic locking.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Integration Tests:
     - Test dos editores modificando mismo documento simultáneamente
     - Test segundo save lanza OptimisticLockException
     - Test múltiples consultants creando propuestas simultáneamente
     - Test todas las propuestas se crean exitosamente
   - Property Tests:
     - **Property 62**: *For any* dos editores modificando misma sección, sistema debe detectar conflicto y rechazar segundo save (**Validates: Property 62**)
     - **Property 63**: *For any* múltiples consultants creando propuestas, todas deben crearse independientemente (**Validates: Property 63**)
   
2. **Green Phase - Implement Minimum Code**:
   - Verificar que `@Version` está en Project entity
   - Implementar manejo de `OptimisticLockException` en servicios
   - Retornar HTTP 409 Conflict con mensaje apropiado
   - Verificar que creación de propuestas no tiene conflictos

3. **Refactor Phase**:
   - Implementar retry logic para operaciones idempotentes
   - Mejorar mensajes de error con información de conflicto
   - Agregar logging de conflictos

**Acceptance Criteria**:
- [ ] Todos los tests de integración pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Conflictos se detectan correctamente
- [ ] Propuestas se crean independientemente

**Related Requirements**: 18.1, 18.2, 18.3, 18.4 (Concurrent Access Handling)
**Related Properties**: Property 62, 63

---

## Milestone 13: Data Integrity & Audit

### Task 13.1: Implement Referential Integrity Constraints

**Descripción**: Verificar y agregar constraints de integridad referencial en base de datos.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Integration Tests:
     - Test intentar crear propuesta sin proyecto lanza excepción
     - Test intentar crear rol sin usuario lanza excepción
     - Test intentar eliminar workspace con proyectos lanza excepción
     - Test eliminar workspace vacío funciona
   - Property Tests:
     - **Property 59**: *For any* creación de entidad, sistema debe prevenir entidades huérfanas (**Validates: Property 59**)
     - **Property 60**: *For any* workspace con proyectos, eliminación debe rechazarse (**Validates: Property 60**)
   
2. **Green Phase - Implement Minimum Code**:
   - Agregar foreign keys en todas las relaciones
   - Agregar constraint para prevenir eliminación de workspace con proyectos
   - Implementar validaciones en servicios antes de operaciones

3. **Refactor Phase**:
   - Implementar soft deletes para entidades importantes
   - Agregar cascade rules apropiadas
   - Mejorar mensajes de error

**Acceptance Criteria**:
- [ ] Todos los tests de integración pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Constraints de BD funcionan correctamente
- [ ] Validaciones en servicios funcionan

**Related Requirements**: 17.3, 17.4 (Data Integrity)
**Related Properties**: Property 59, 60

### Task 13.2: Implement Comprehensive Audit Logging

**Descripción**: Implementar logging de auditoría para operaciones críticas.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests:
     - Test transición de estado registra audit log
     - Test aprobación de propuesta registra audit log
     - Test rechazo de propuesta registra audit log
     - Test asignación de rol registra audit log
   - Property Tests:
     - **Property 61**: *For any* transición de estado o resolución de propuesta, debe crearse audit log entry (**Validates: Property 61**)
   
2. **Green Phase - Implement Minimum Code**:
   - Crear tabla `audit_logs` con campos: entityType, entityId, action, userId, timestamp, details
   - Crear `AuditLogRepository`
   - Implementar `AuditService` para registrar eventos
   - Integrar con servicios existentes

3. **Refactor Phase**:
   - Implementar anotación `@Audited` para automatizar logging
   - Agregar índices en audit_logs
   - Implementar retención de logs (archivar logs antiguos)

**Acceptance Criteria**:
- [ ] Todos los tests unitarios pasan
- [ ] Property tests pasan (100+ iteraciones)
- [ ] Audit logs se crean correctamente
- [ ] Logs contienen información completa

**Related Requirements**: 17.5 (Audit Logs)
**Related Properties**: Property 61

---

## Milestone 14: Integration & End-to-End Tests

### Task 14.1: Implement End-to-End Workflow Tests

**Descripción**: Crear tests end-to-end que validen flujos completos del sistema.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Integration Tests:
     - Test flujo completo: crear workspace → crear proyecto → asignar roles → editar → consultar → aprobar propuestas → publicar
     - Test flujo de múltiples ciclos: publicar → editar → consultar → publicar nuevamente
     - Test flujo de merge de propuestas
     - Test flujo de exportación y compartir link
   
2. **Green Phase - Implement Minimum Code**:
   - Crear clase `EndToEndWorkflowTest`
   - Implementar tests que ejercitan múltiples servicios
   - Usar TestContainers para BD real
   - Verificar estado final del sistema

3. **Refactor Phase**:
   - Extraer helpers para setup de datos de test
   - Implementar fixtures reutilizables
   - Mejorar assertions

**Acceptance Criteria**:
- [ ] Todos los tests end-to-end pasan
- [ ] Flujos completos funcionan correctamente
- [ ] Tests son reproducibles

**Related Requirements**: Todos los requirements

### Task 14.2: Implement Inter-Service Communication Tests

**Descripción**: Crear tests para validar comunicación entre Workspace Service y Project Service.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Integration Tests:
     - Test Project Service valida membresía en workspace via Workspace Service
     - Test Project Service valida existencia de usuario via Workspace Service
     - Test manejo de errores cuando Workspace Service no disponible
   
2. **Green Phase - Implement Minimum Code**:
   - Implementar `WorkspaceClient` en Project Service usando WebClient
   - Implementar endpoints en Workspace Service para validación
   - Implementar circuit breaker para resiliencia
   - Implementar retry logic

3. **Refactor Phase**:
   - Agregar caché para validaciones frecuentes
   - Implementar fallback strategies
   - Mejorar logging de comunicación inter-servicio

**Acceptance Criteria**:
- [ ] Todos los tests de integración pasan
- [ ] Comunicación inter-servicio funciona
- [ ] Circuit breaker funciona correctamente

**Related Requirements**: 1.2, 3.1 (Workspace Member Validation)

### Task 14.3: Implement Performance and Load Tests

**Descripción**: Crear tests de performance y carga para operaciones críticas.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Performance Tests:
     - Test crear proyecto < 200ms
     - Test transicionar estado < 100ms
     - Test crear propuesta < 150ms
     - Test generar versión < 500ms
     - Test exportar PDF < 2s
   - Load Tests:
     - Test sistema maneja 100 usuarios concurrentes
     - Test sistema maneja 1000 propuestas en un proyecto
   
2. **Green Phase - Implement Minimum Code**:
   - Crear tests con JMH para micro-benchmarking
   - Crear tests con Gatling para load testing
   - Medir tiempos de respuesta
   - Identificar cuellos de botella

3. **Refactor Phase**:
   - Optimizar queries lentas
   - Agregar índices faltantes
   - Implementar caché donde apropiado
   - Optimizar generación de PDF

**Acceptance Criteria**:
- [ ] Performance tests pasan
- [ ] Load tests pasan
- [ ] Sistema cumple SLAs definidos

**Related Requirements**: Performance requirements (implícitos)

---

## Milestone 15: Documentation & Deployment

### Task 15.1: Generate API Documentation with OpenAPI/Swagger

**Descripción**: Generar documentación interactiva de API usando Swagger.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Integration Tests:
     - Test GET /swagger-ui.html retorna página de documentación
     - Test /v3/api-docs retorna especificación OpenAPI válida
   
2. **Green Phase - Implement Minimum Code**:
   - Agregar dependencia springdoc-openapi
   - Configurar Swagger UI
   - Agregar anotaciones `@Operation`, `@ApiResponse` en controllers
   - Configurar información de API (título, versión, descripción)

3. **Refactor Phase**:
   - Agregar ejemplos de requests/responses
   - Documentar códigos de error
   - Agregar autenticación JWT en Swagger UI

**Acceptance Criteria**:
- [ ] Swagger UI accesible
- [ ] Documentación completa y precisa
- [ ] Ejemplos funcionan correctamente

**Related Requirements**: Documentation (implícito)

### Task 15.2: Create Docker Compose Configuration

**Descripción**: Crear configuración Docker Compose para ejecutar sistema completo.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Integration Tests:
     - Test `docker-compose up` inicia todos los servicios
     - Test servicios pueden comunicarse entre sí
     - Test BD PostgreSQL está accesible
   
2. **Green Phase - Implement Minimum Code**:
   - Crear `docker-compose.yml` con:
     - PostgreSQL para Workspace Service
     - PostgreSQL para Project Service
     - Workspace Service
     - Project Service
   - Crear Dockerfiles para cada servicio
   - Configurar redes y volúmenes

3. **Refactor Phase**:
   - Agregar health checks
   - Configurar variables de entorno
   - Agregar profiles para dev/prod

**Acceptance Criteria**:
- [ ] Docker Compose inicia sistema completo
- [ ] Servicios se comunican correctamente
- [ ] BD persiste datos

**Related Requirements**: Deployment (implícito)

### Task 15.3: Create README and Setup Instructions

**Descripción**: Crear documentación completa para desarrolladores.

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Manual Tests:
     - Seguir instrucciones de README desde cero
     - Verificar que todos los pasos funcionan
   
2. **Green Phase - Implement Minimum Code**:
   - Crear `README.md` con:
     - Descripción del proyecto
     - Requisitos previos
     - Instrucciones de instalación
     - Instrucciones para ejecutar tests
     - Instrucciones para ejecutar aplicación
     - Arquitectura del sistema
     - Endpoints principales
   - Crear `CONTRIBUTING.md` con guías de contribución
   - Crear `TESTING.md` con estrategia de testing

3. **Refactor Phase**:
   - Agregar diagramas de arquitectura
   - Agregar ejemplos de uso
   - Agregar troubleshooting guide

**Acceptance Criteria**:
- [ ] README es completo y claro
- [ ] Instrucciones funcionan correctamente
- [ ] Documentación está actualizada

**Related Requirements**: Documentation (implícito)

---

## Checkpoint Final

### Task 16.1: Run Full Test Suite and Verify Coverage

**Descripción**: Ejecutar suite completa de tests y verificar cobertura de código.

**Enfoque TDD**:
1. **Verification**:
   - Ejecutar todos los unit tests
   - Ejecutar todos los property tests (100+ iteraciones cada uno)
   - Ejecutar todos los integration tests
   - Ejecutar todos los end-to-end tests
   - Generar reporte de cobertura con JaCoCo
   - Verificar cobertura > 80%

2. **Fix Issues**:
   - Corregir tests fallidos
   - Agregar tests para código no cubierto
   - Refactorizar código con baja cobertura

3. **Documentation**:
   - Documentar decisiones de diseño
   - Actualizar diagramas si es necesario
   - Crear guía de troubleshooting

**Acceptance Criteria**:
- [ ] Todos los tests pasan (unit, property, integration, e2e)
- [ ] Cobertura de código > 80%
- [ ] No hay warnings críticos
- [ ] Documentación está completa

**Related Requirements**: Todos los requirements
**Related Properties**: Todas las properties (1-86)

### Task 16.2: Final System Validation

**Descripción**: Validación final del sistema completo contra todos los requirements.

**Enfoque TDD**:
1. **Validation**:
   - Revisar cada requirement y verificar implementación
   - Revisar cada property y verificar test correspondiente
   - Ejecutar flujos end-to-end manualmente
   - Verificar performance bajo carga

2. **Checklist**:
   - [ ] Todos los 23 requirements implementados
   - [ ] Todas las 86 properties tienen tests
   - [ ] Todos los endpoints REST funcionan
   - [ ] Autenticación y autorización funcionan
   - [ ] Notificaciones funcionan
   - [ ] Versionado funciona
   - [ ] Exportación a PDF funciona
   - [ ] Links compartibles funcionan
   - [ ] Búsqueda y filtrado funcionan
   - [ ] Concurrencia se maneja correctamente
   - [ ] Audit logs se registran
   - [ ] Documentación está completa

3. **Sign-off**:
   - Obtener aprobación del usuario
   - Documentar issues conocidos
   - Crear plan de deployment

**Acceptance Criteria**:
- [ ] Sistema cumple todos los requirements
- [ ] Todas las properties están validadas
- [ ] Sistema está listo para deployment
- [ ] Usuario aprueba el sistema

**Related Requirements**: Todos los requirements (1-23)
**Related Properties**: Todas las properties (1-86)

---

## Notes

- **TDD Estricto**: Cada tarea sigue Red-Green-Refactor sin excepciones
- **Property-Based Testing**: Mínimo 100 iteraciones por property test
- **Cobertura**: Target de 80%+ line coverage
- **Milestones**: Cada milestone es deployable independientemente
- **Checkpoints**: Validar progreso al final de cada milestone
- **Documentación**: Mantener actualizada durante desarrollo
- **Code Review**: Revisar código antes de merge a main branch

