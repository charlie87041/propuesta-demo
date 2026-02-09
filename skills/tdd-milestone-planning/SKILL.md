# TDD Milestone Planning Skill

## Purpose
Este skill define las pautas para crear milestones e issues de implementación siguiendo Test-Driven Development (TDD) estricto, con enfoque en property-based testing y documentación continua.

## Core Principles

### 1. Test-Driven Development (TDD) Cycle
Cada tarea DEBE seguir el ciclo Red-Green-Refactor:

**Red Phase - Write Failing Tests**:
- Escribir tests que fallen ANTES de implementar código
- Incluir múltiples tipos de tests según corresponda:
  - **Unit Tests**: Lógica de negocio aislada, casos específicos
  - **Integration Tests**: Interacción con BD (TestContainers), servicios externos
  - **Property Tests**: Propiedades universales con 100+ iteraciones
  - **Contract Tests**: Contratos de API REST (formato JSON, campos requeridos)

**Green Phase - Implement Minimum Code**:
- Implementar SOLO el código mínimo necesario para pasar los tests
- No agregar funcionalidad extra "por si acaso"
- Mantener implementación simple y directa

**Refactor Phase**:
- Mejorar código manteniendo tests verdes
- Extraer duplicación, mejorar nombres, optimizar
- Agregar logging, validaciones adicionales, documentación inline

### 2. Property-Based Testing (PBT)
Cuando existan propiedades de corrección en el design document:

- **Mapear propiedades a tests**: Cada property del design.md debe tener un property test correspondiente
- **Iteraciones mínimas**: 100+ iteraciones por property test
- **Referencias explícitas**: Comentar en el test qué property valida (ej: `// Validates: Property 17`)
- **Generadores inteligentes**: Crear generadores que produzcan datos válidos del dominio
- **Frameworks**: Usar jqwik (Java), Hypothesis (Python), fast-check (JavaScript), etc.

### 3. Milestone Organization

#### Estructura Lógica
Organizar milestones siguiendo dependencias arquitectónicas:

1. **Infrastructure First**: Setup, build tools, testing infrastructure
2. **Shared Modules**: Código común (auth, security, utils)
3. **Core Services**: Servicios fundamentales (User, Workspace)
4. **Domain Services**: Lógica de negocio principal (Project, Roles, Proposals)
5. **Advanced Features**: Features adicionales (Export, Search, Notifications)
6. **Integration & Testing**: Tests end-to-end, performance
7. **Documentation & Deployment**: Docs finales, CI/CD, deployment

#### Granularidad de Tareas
- **1 tarea = 1 entidad/componente/feature**: No mezclar múltiples entidades en una tarea
- **Orden de implementación**: Entity → Repository → Service → Controller
- **Dependencias claras**: Cada milestone debe listar prerequisitos

### 4. Issue Structure

Cada issue DEBE contener:

```markdown
## Issue X.Y: [Título Descriptivo]

**Labels**: `enhancement`, `backend`, `testing`, [otros]

### Description
[1-2 párrafos explicando QUÉ se implementa y POR QUÉ]

### TDD Approach
**Red Phase**: [Tipos de tests a escribir]
**Green Phase**: [Implementación mínima]
**Refactor Phase**: [Mejoras a considerar]

### Tasks
- [ ] Write failing unit tests
  - [ ] [Escenario específico 1]
  - [ ] [Escenario específico 2]
- [ ] Write failing integration tests
  - [ ] [Escenario específico 1]
- [ ] Write property tests (si aplica)
  - [ ] Property N: [Descripción] (**Validates: Property N**)
- [ ] Implement [componente]
- [ ] [Pasos de implementación]
- [ ] Refactor: [mejoras]
- [ ] Write inline documentation (Javadoc/JSDoc)
- [ ] Update API documentation (si aplica)

### Acceptance Criteria
- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] Property tests pass (100+ iterations) [si aplica]
- [ ] Code coverage > 80%
- [ ] [Criterio funcional específico]
- [ ] Inline documentation complete
- [ ] API documentation updated (si aplica)

### Related Requirements
- Requirements X.Y ([Nombre del requirement])

### Related Properties (si aplica)
- Property N: [Descripción]
```

### 5. Documentation Strategy

#### Documentation as You Go
La documentación NO es una fase final, sino parte integral de cada tarea:

**Inline Documentation** (Obligatorio en cada tarea):
- Javadoc/JSDoc para clases públicas y métodos públicos
- Comentarios explicativos para lógica compleja
- Ejemplos de uso en docstrings
- Incluir en **Refactor Phase** de cada tarea

**API Documentation** (Cuando se crean endpoints):
- OpenAPI/Swagger annotations en controllers
- Ejemplos de request/response
- Códigos de error documentados
- Incluir en tareas de Controller

**Architecture Documentation** (Por milestone):
- Diagrama de componentes del milestone
- Decisiones arquitectónicas tomadas
- Patrones de diseño aplicados
- Crear en tarea final de cada milestone

**README Updates** (Incremental):
- Actualizar README.md con nuevas features
- Instrucciones de setup si cambian
- Ejemplos de uso de nuevas APIs
- Incluir en tareas que agregan features user-facing

#### Documentation Milestones
Agregar tareas específicas de documentación:

**Durante desarrollo** (en cada milestone):
- Inline docs en Refactor Phase
- API docs en tareas de Controller
- Architecture docs al final del milestone

**Milestone final de documentación**:
- Consolidar documentación dispersa
- Crear guías de usuario
- Documentar deployment
- Crear troubleshooting guide

### 6. Testing Infrastructure

#### TestContainers (Java/Spring Boot)
- Usar para tests de integración con BD real
- Configurar singleton container para performance
- Crear clase base `AbstractIntegrationTest`

#### Test Organization
```
src/
├── main/java/
│   └── com/project/
│       ├── entity/
│       ├── repository/
│       ├── service/
│       └── controller/
└── test/java/
    └── com/project/
        ├── entity/          # Unit tests
        ├── repository/      # Integration tests
        ├── service/         # Unit + Property tests
        ├── controller/      # Integration + Contract tests
        └── properties/      # Property-based tests
```

### 7. Acceptance Criteria Standards

Cada tarea DEBE incluir:
- [ ] **All unit tests pass**: Tests de lógica aislada
- [ ] **All integration tests pass**: Tests con BD/servicios
- [ ] **Property tests pass (100+ iterations)**: Si hay properties
- [ ] **Code coverage > 80%**: Mínimo de cobertura
- [ ] **[Criterio funcional]**: Específico de la feature
- [ ] **Inline documentation complete**: Javadoc/JSDoc
- [ ] **API documentation updated**: Si se crean/modifican endpoints
- [ ] **No regressions**: Tests existentes siguen pasando

### 8. Labels and Categorization

**Labels estándar**:
- `enhancement`: Nueva funcionalidad
- `backend`: Código backend
- `frontend`: Código frontend
- `testing`: Tests (unit, integration, property)
- `database`: Cambios en BD o entidades
- `api`: Endpoints REST
- `security`: Seguridad y autenticación
- `infrastructure`: Setup y configuración
- `business-logic`: Lógica de negocio
- `documentation`: Documentación
- `refactoring`: Refactoring sin cambio funcional

### 9. Estimation Guidelines

**Por tipo de tarea**:
- Entity + Repository: 0.5-1 día
- Service (simple): 1-2 días
- Service (complejo con state machine): 2-3 días
- Controller: 1-2 días
- Property tests (por property): 0.5-1 día
- Integration tests completos: 1-2 días
- Documentation milestone: 1-2 semanas

**Por milestone**:
- Infrastructure: 1-2 semanas
- Core services: 2-3 semanas
- Domain services: 4-6 semanas
- Advanced features: 2-3 semanas
- Integration & docs: 2-3 semanas

### 10. Quality Gates

Antes de cerrar un issue:
1. ✅ Todos los tests pasan (unit, integration, property)
2. ✅ Cobertura > 80%
3. ✅ No warnings de linter/compiler
4. ✅ Code review aprobado
5. ✅ Documentación inline completa
6. ✅ API docs actualizados (si aplica)
7. ✅ CI/CD pipeline verde

Antes de cerrar un milestone:
1. ✅ Todos los issues completados
2. ✅ Architecture documentation creada
3. ✅ README actualizado con nuevas features
4. ✅ Integration tests entre componentes del milestone pasan
5. ✅ Demo funcional de las features del milestone

## Example Application

### Ejemplo: Milestone "User Management"

**Issues**:
1. **User Entity and Repository**
   - Red: Unit tests (constraints), Integration tests (CRUD), Property tests (round-trip)
   - Green: Entity + Repository
   - Refactor: Indexes, equals/hashCode, Javadoc
   - Docs: Javadoc completo en User entity

2. **UserService with Registration**
   - Red: Unit tests (validation), Property tests (register→authenticate)
   - Green: Service methods
   - Refactor: Extract validations, logging
   - Docs: Javadoc en métodos públicos

3. **UserController REST API**
   - Red: Integration tests (endpoints), Contract tests (JSON)
   - Green: Controller + DTOs
   - Refactor: Mapper class, validation
   - Docs: OpenAPI annotations, ejemplos de uso

4. **User Management Architecture Documentation**
   - Diagrama de componentes (Entity→Repository→Service→Controller)
   - Decisiones: BCrypt para passwords, JWT para tokens
   - Patrones: Repository pattern, DTO pattern
   - Update README con endpoints de user management

## Usage Instructions

### For AI Agents

Cuando generes milestones e issues:

1. **Analizar requirements y design**: Identificar entidades, servicios, propiedades
2. **Organizar en milestones lógicos**: Seguir orden de dependencias
3. **Crear issues con estructura estándar**: Usar template de arriba
4. **Mapear properties a tests**: Cada property → property test
5. **Incluir documentación en cada tarea**: Inline docs en Refactor, API docs en Controllers
6. **Agregar milestone de documentación final**: Consolidar y crear guías
7. **Definir acceptance criteria claros**: Incluir tests, coverage, docs
8. **Estimar realísticamente**: Usar guidelines de arriba

### For Developers

Cuando implementes una tarea:

1. **Leer requirements y properties relacionadas**: Entender QUÉ y POR QUÉ
2. **Red Phase**: Escribir TODOS los tests primero (unit, integration, property)
3. **Green Phase**: Implementar código mínimo para pasar tests
4. **Refactor Phase**: Mejorar código + escribir Javadoc/JSDoc
5. **Update API docs**: Si creaste/modificaste endpoints
6. **Verificar acceptance criteria**: Todos los checkboxes verdes
7. **Code review**: Antes de merge

## Anti-Patterns to Avoid

❌ **NO escribir código antes que tests**: Viola TDD  
❌ **NO mezclar múltiples entidades en una tarea**: Dificulta testing  
❌ **NO omitir property tests**: Son críticos para corrección  
❌ **NO dejar documentación para el final**: Documentar mientras codificas  
❌ **NO hacer tareas gigantes**: Máximo 3-4 días por tarea  
❌ **NO ignorar cobertura**: 80% es mínimo  
❌ **NO hacer refactor sin tests verdes**: Puede romper funcionalidad  
❌ **NO documentar solo código público**: Lógica compleja también necesita comentarios

## Success Metrics

- ✅ 100% de tareas siguen ciclo Red-Green-Refactor
- ✅ 100% de properties tienen property tests con 100+ iteraciones
- ✅ >80% code coverage en todos los módulos
- ✅ 100% de clases públicas tienen Javadoc/JSDoc
- ✅ 100% de endpoints tienen OpenAPI docs
- ✅ 0 tests rotos en main branch
- ✅ Documentación actualizada en cada milestone
- ✅ Architecture docs completos al final de cada milestone

## References

- **TDD**: Kent Beck - "Test Driven Development: By Example"
- **Property-Based Testing**: "Property-Based Testing with PropEr, Erlang, and Elixir"
- **Clean Code**: Robert C. Martin - "Clean Code"
- **Documentation**: "Docs as Code" methodology

# Skill: tdd-milestone-planning

## Purpose
Planificar milestones e issues con TDD estricto y property-based testing.

## When to Use
- Al descomponer proyectos grandes en milestones.
- Para definir issues con criterios de aceptación claros.

## Usage
- Sigue la estructura de issue propuesta.
- Mapea properties a property tests con 100+ iteraciones.
- Incluye documentación en cada tarea.

## Examples
- Milestone de "User Management" con issues por entidad/servicio/controlador.
- Checklist de aceptación con cobertura > 80%.

## Related Skills
- tdd-workflow
- springboot-tdd
