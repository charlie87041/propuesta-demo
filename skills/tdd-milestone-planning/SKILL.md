# TDD Milestone Planning Skill

## Purpose
This skill defines guidelines for creating milestones and implementation issues following strict Test-Driven Development (TDD), with a focus on property-based testing and continuous documentation.

## Core Principles

### 1. Test-Driven Development (TDD) Cycle
Each task MUST follow the Red-Green-Refactor cycle:

**Red Phase - Write Failing Tests**:
- Write tests that fail BEFORE implementing code
- Include multiple test types as appropriate:
  - **Unit Tests**: Isolated business logic, specific cases
  - **Integration Tests**: Interaction with DB (TestContainers), external services
  - **Property Tests**: Universal properties with 100+ iterations
  - **Contract Tests**: REST API contracts (JSON format, required fields)

**Green Phase - Implement Minimum Code**:
- Implement ONLY the minimum code necessary to pass the tests
- Don't add extra functionality "just in case"
- Keep implementation simple and direct

**Refactor Phase**:
- Improve code while keeping tests green
- Extract duplication, improve names, optimize
- Add logging, additional validations, inline documentation

### 2. Property-Based Testing (PBT)
When correctness properties exist in the design document:

- **Map properties to tests**: Each property from design.md should have a corresponding property test
- **Minimum iterations**: 100+ iterations per property test
- **Explicit references**: Comment in the test which property it validates (e.g., `// Validates: Property 17`)
- **Smart generators**: Create generators that produce valid domain data
- **Frameworks**: Use jqwik (Java), Hypothesis (Python), fast-check (JavaScript), etc.

### 3. Milestone Organization

#### Logical Structure
Organize milestones following architectural dependencies:

1. **Infrastructure First**: Setup, build tools, testing infrastructure
2. **Shared Modules**: Common code (auth, security, utils)
3. **Core Services**: Fundamental services (User, Workspace)
4. **Domain Services**: Main business logic (Project, Roles, Proposals)
5. **Advanced Features**: Additional features (Export, Search, Notifications)
6. **Integration & Testing**: End-to-end tests, performance
7. **Documentation & Deployment**: Final docs, CI/CD, deployment

#### Task Granularity
- **1 task = 1 entity/component/feature**: Don't mix multiple entities in one task
- **Implementation order**: Entity → Repository → Service → Controller
- **Clear dependencies**: Each milestone should list prerequisites

### 4. Issue Structure

Cada issue DEBE contener:

```markdown
## Issue X.Y: [Descriptive Title]

**Labels**: `enhancement`, `backend`, `testing`, [otros]

### Description
[1-2 paragraphs explaining WHAT is implemented and WHY]

### TDD Approach
**Red Phase**: [Tipos de tests a escribir]
**Green Phase**: [Minimal implementation]
**Refactor Phase**: [Mejoras a considerar]

### Tasks
- [ ] Write failing unit tests
  - [ ] [Specific scenario 1]
  - [ ] [Specific scenario 2]
- [ ] Write failing integration tests
  - [ ] [Specific scenario 1]
- [ ] Write property tests (si aplica)
  - [ ] Property N: [Description] (**Validates: Property N**)
- [ ] Implement [componente]
- [ ] [Implementation steps]
- [ ] Refactor: [mejoras]
- [ ] Write inline documentation (Javadoc/JSDoc)
- [ ] Update API documentation (si aplica)

### Acceptance Criteria
- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] Property tests pass (100+ iterations) [si aplica]
- [ ] Code coverage > 80%
- [ ] [Specific functional criterion]
- [ ] Inline documentation complete
- [ ] API documentation updated (si aplica)

### Related Requirements
- Requirements X.Y ([Nombre del requirement])

### Related Properties (si aplica)
- Property N: [Description]
```

### 5. Documentation Strategy

#### Documentation as You Go
Documentation is NOT a final phase, but an integral part of each task:

**Inline Documentation** (Obligatorio en cada tarea):
- Javadoc/JSDoc for public classes and public methods
- Explanatory comments for complex logic
- Ejemplos de uso en docstrings
- Incluir en **Refactor Phase** de cada tarea

**API Documentation** (Cuando se crean endpoints):
- OpenAPI/Swagger annotations en controllers
- Ejemplos de request/response
- Documented error codes
- Incluir en tareas de Controller

**Architecture Documentation** (By milestone):
- Component diagram del milestone
- Architectural decisions made
- Design patterns applied
- Crear en tarea final de cada milestone

**README Updates** (Incremental):
- Actualizar README.md con nuevas features
- Instrucciones de setup si cambian
- Ejemplos de uso de nuevas APIs
- Include in tasks that add user-facing features

#### Documentation Milestones
Add specific documentation tasks:

**During development** (in each milestone):
- Inline docs in Refactor Phase
- API docs in Controller tasks
- Architecture docs at the end of the milestone

**Final documentation milestone**:
- Consolidate scattered documentation
- Create user guides
- Document deployment
- Create troubleshooting guide

### 6. Testing Infrastructure

#### TestContainers (Java/Spring Boot)
- Use for integration tests with real DB
- Configure singleton container for performance
- Create base class `AbstractIntegrationTest`

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

Each task MUST include:
- [ ] **All unit tests pass**: Isolated logic tests
- [ ] **All integration tests pass**: Tests with DB/services
- [ ] **Property tests pass (100+ iterations)**: If properties exist
- [ ] **Code coverage > 80%**: Minimum coverage
- [ ] **[Functional criterion]**: Specific to the feature
- [ ] **Inline documentation complete**: Javadoc/JSDoc
- [ ] **API documentation updated**: If endpoints are created/modified
- [ ] **No regressions**: Existing tests continue to pass

### 8. Labels and Categorization

**Standard labels**:
- `enhancement`: New functionality
- `backend`: Backend code
- `frontend`: Frontend code
- `testing`: Tests (unit, integration, property)
- `database`: DB or entity changes
- `api`: REST endpoints
- `security`: Security and authentication
- `infrastructure`: Setup and configuration
- `business-logic`: Business logic
- `documentation`: Documentation
- `refactoring`: Refactoring without functional change

### 9. Estimation Guidelines

**By task type**:
- Entity + Repository: 0.5-1 day
- Service (simple): 1-2 days
- Service (complejo con state machine): 2-3 days
- Controller: 1-2 days
- Property tests (por property): 0.5-1 day
- Integration tests completos: 1-2 days
- Documentation milestone: 1-2 weeks

**By milestone**:
- Infrastructure: 1-2 weeks
- Core services: 2-3 weeks
- Domain services: 4-6 weeks
- Advanced features: 2-3 weeks
- Integration & docs: 2-3 weeks

### 10. Quality Gates

Before closing an issue:
1. ✅ All tests pass (unit, integration, property)
2. ✅ Coverage > 80%
3. ✅ No linter/compiler
4. ✅ Code review approved
5. ✅ Inline documentation complete
6. ✅ API docs updated (if applicable)
7. ✅ CI/CD pipeline green

Before closing a milestone:
1. ✅ All issues completed
2. ✅ Architecture documentation created
3. ✅ README updated with new features
4. ✅ Integration tests between milestone components pass
5. ✅ Functional demo of milestone features

## Example Application

### Example: Milestone "User Management"

**Issues**:
1. **User Entity and Repository**
   - Red: Unit tests (constraints), Integration tests (CRUD), Property tests (round-trip)
   - Green: Entity + Repository
   - Refactor: Indexes, equals/hashCode, Javadoc
   - Docs: Complete Javadoc on User entity

2. **UserService with Registration**
   - Red: Unit tests (validation), Property tests (register→authenticate)
   - Green: Service methods
   - Refactor: Extract validations, logging
   - Docs: Javadoc on public methods

3. **UserController REST API**
   - Red: Integration tests (endpoints), Contract tests (JSON)
   - Green: Controller + DTOs
   - Refactor: Mapper class, validation
   - Docs: OpenAPI annotations, usage examples

4. **User Management Architecture Documentation**
   - Component diagram (Entity→Repository→Service→Controller)
   - Decisions: BCrypt for passwords, JWT for tokens
   - Patterns: Repository pattern, DTO pattern
   - Update README with endpoints for user management

## Usage Instructions

### For AI Agents

When generating milestones and issues:

1. **Analizar requirements y design**: Identify entities, services, properties
2. **Organize into logical milestones**: Follow dependency order
3. **Create issues with standard structure**: Use template above
4. **Mapear properties a tests**: Each property → property test
5. **Include documentation in each task**: Inline docs in Refactor, API docs in Controllers
6. **Add final documentation milestone**: Consolidate and create guides
7. **Definir acceptance criteria claros**: Include tests, coverage, docs
8. **Estimate realistically**: Use guidelines above

### For Developers

When implementing a task:

1. **Leer requirements y properties relacionadas**: Understand WHAT and WHY
2. **Red Phase**: Write ALL tests first (unit, integration, property)
3. **Green Phase**: Implement minimum code to pass tests
4. **Refactor Phase**: Improve code + write Javadoc/JSDoc
5. **Update API docs**: If you created/modified endpoints
6. **Verificar acceptance criteria**: All checkboxes green
7. **Code review**: Before merge

## Anti-Patterns to Avoid

❌ **DON'T write code before tests**: Violates TDD  
❌ **DON'T mix multiple entities in one task**: Makes testing difficult  
❌ **DON'T skip property tests**: They are critical for correctness  
❌ **DON'T leave documentation for the end**: Document while coding  
❌ **DON'T create giant tasks**: Maximum 3-4 days per task  
❌ **DON'T ignore coverage**: 80% is minimum  
❌ **DON'T refactor without green tests**: Can break functionality  
❌ **DON'T document only public code**: Complex logic also needs comments

## Success Metrics

- ✅ 100% de tareas siguen ciclo Red-Green-Refactor
- ✅ 100% de properties tienen property tests con 100+ iteraciones
- ✅ >80% code coverage in all modules
- ✅ 100% of public classes have Javadoc/JSDoc
- ✅ 100% de endpoints tienen OpenAPI docs
- ✅ 0 tests rotos en main branch
- ✅ Documentation updated in each milestone
- ✅ Architecture docs completos al final de cada milestone

## References

- **TDD**: Kent Beck - "Test Driven Development: By Example"
- **Property-Based Testing**: "Property-Based Testing with PropEr, Erlang, and Elixir"
- **Clean Code**: Robert C. Martin - "Clean Code"
- **Documentation**: "Docs as Code" methodology

# Skill: tdd-milestone-planning

## Purpose
Plan milestones and issues with strict TDD and property-based testing.

## When to Use
- When breaking down large projects into milestones.
- To define issues with clear acceptance criteria.

## Usage
- Follow the proposed issue structure.
- Map properties to property tests with 100+ iterations.
- Include documentation in each task.

## Examples
- "User Management" milestone with issues per entity/service/controller.
- Acceptance checklist with coverage > 80%.

## Related Skills
- tdd-workflow
- springboot-tdd
