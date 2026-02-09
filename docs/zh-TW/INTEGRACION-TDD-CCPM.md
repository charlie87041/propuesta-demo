# Guía Completa: Integración TDD Milestone Planning + CCPM

Esta guía explica cómo hacer que los issues generados por CCPM sigan estrictamente tu filosofía de TDD Milestone Planning.

## Tabla de Contenidos

1. [Configuración Inicial](#configuración-inicial)
2. [Workflow Completo](#workflow-completo)
3. [Opción A: Automática (Recomendada)](#opción-a-automática)
4. [Opción B: Semi-automática](#opción-b-semi-automática)
5. [Opción C: Manual](#opción-c-manual)
6. [Ejemplos Prácticos](#ejemplos-prácticos)
7. [Troubleshooting](#troubleshooting)

---

## Configuración Inicial

### 1. Instalar Componentes

```bash
# 1. Clonar everything-claude-code
git clone https://github.com/affaan-m/everything-claude-code.git
cd everything-claude-code

# 2. Instalar tu skill TDD
cp /path/to/tdd-milestone-planning.md ~/.claude/skills/

# 3. Instalar el agente planner-tdd modificado
cp planner-tdd.md ~/.claude/agents/

# 4. Instalar el comando plan-tdd
cp plan-tdd.md ~/.claude/commands/

# 5. Instalar CCPM
curl -sSL https://automaze.io/ccpm/install | bash

# 6. Configurar GitHub token para CCPM
export GITHUB_TOKEN=ghp_your_token_here
```

### 2. Verificar Instalación

```bash
# Verificar que el agente está disponible
claude --list-agents | grep planner-tdd

# Verificar que el skill está disponible
claude --list-skills | grep tdd-milestone-planning

# Verificar CCPM
pm --version
```

---

## Workflow Completo

```
[Descripción] 
    ↓
[/plan-tdd] → [Planner-TDD Agent]
    ↓
[Plan TDD con Red-Green-Refactor]
    ↓
[Script: tdd-to-prd.js] (automático)
    ↓
[PRD compatible con CCPM]
    ↓
[/pm:prd-new, /pm:prd-parse, /pm:epic-oneshot]
    ↓
[GitHub Issues con estructura TDD]
    ↓
[Desarrollo: /pm:issue-start N + /tdd]
```

---

## Opción A: Automática (Recomendada)

Esta opción usa el script `tdd-to-prd.js` para convertir automáticamente el plan TDD a formato CCPM.

### Paso 1: Generar Plan TDD

```bash
# En Claude Code
/plan-tdd "Sistema de autenticación con JWT, reset de password y verificación de email"
```

El agente `planner-tdd` generará un plan estructurado:

```markdown
### Milestone 1: Infrastructure Setup (1-2 weeks)

**Goal**: Establish testing infrastructure

**Issues**:
- Issue 1.1: TestContainers setup (1 day)
  - Red: Integration test that requires DB
  - Green: Configure TestContainers
  - Refactor: Singleton pattern, Javadoc

- Issue 1.2: Property testing framework (0.5 days)
  - Red: Simple property test
  - Green: Add jqwik/Hypothesis
  - Refactor: Generator utilities, docs

### Milestone 2: User Management (2-3 weeks)

**Properties to validate**:
- Property 17: Registration → Authentication succeeds
- Property 23: Password hashing is irreversible

**Issues**:
- Issue 2.1: User Entity and Repository (1 day)
  - Red: Unit tests (constraints), Integration tests (CRUD), Property test (round-trip)
  - Green: Entity + Repository
  - Refactor: Indexes, equals/hashCode, Javadoc
  
[...]
```

### Paso 2: Guardar el Plan

```bash
# Copiar la salida del agente a un archivo
/save-output user-auth-plan.md
```

### Paso 3: Convertir a PRD (Automático)

```bash
# Ejecutar el script de conversión
node tdd-to-prd.js user-auth-plan.md user-auth

# Output:
# ✅ PRD created: prds/user-auth.md
# Next steps:
# 1. Review and edit: prds/user-auth.md
# 2. Generate issues: /pm:prd-new user-auth
# 3. Parse PRD: /pm:prd-parse user-auth
# 4. Create issues: /pm:epic-oneshot user-auth
```

### Paso 4: Generar Issues en GitHub

```bash
# En Claude Code
/pm:prd-new user-auth
/pm:prd-parse user-auth
/pm:epic-oneshot user-auth

# CCPM creará issues en GitHub con la estructura TDD completa
```

### Paso 5: Desarrollar

```bash
# Iniciar trabajo en un issue
/pm:issue-start 1

# Seguir workflow TDD
/tdd

# Completar issue
/pm:issue-complete 1
```

---

## Opción B: Semi-automática

Si prefieres tener más control sobre el PRD antes de generar issues.

### Paso 1-2: Igual que Opción A

### Paso 3: Convertir y Revisar

```bash
# Convertir a PRD
node tdd-to-prd.js user-auth-plan.md user-auth

# Editar PRD antes de generar issues
code prds/user-auth.md

# Ajustar:
# - Estimaciones
# - Descripciones
# - Criterios de aceptación
# - Propiedades adicionales
```

### Paso 4: Generar Issues

```bash
# Después de revisar y ajustar
/pm:prd-new user-auth
/pm:prd-parse user-auth
/pm:epic-oneshot user-auth
```

---

## Opción C: Manual

Si quieres máximo control o necesitas casos especiales.

### Template de Issue Manual

Crea issues en GitHub usando este template (basado en tu skill):

```markdown
## Issue X.Y: [Título]

**Labels**: enhancement, backend, testing
**Estimate**: [días]

### Description
[Qué se implementa y por qué]

### Related Requirements
- Requirement X.Y: [nombre]

### Related Properties
- Property N: [descripción]

### TDD Approach

**Red Phase** (Write Failing Tests):
- [ ] Unit tests:
  - [ ] Test invalid email format
  - [ ] Test duplicate username rejection
- [ ] Integration tests:
  - [ ] Test user saved to database
  - [ ] Test transaction rollback on error
- [ ] Property tests:
  - [ ] Property 17: Registration → Authentication (100+ iterations)

**Green Phase** (Minimum Implementation):
- [ ] Implement UserService.register()
- [ ] Add email validation
- [ ] Add password hashing

**Refactor Phase** (Improve & Document):
- [ ] Extract validation logic to separate class
- [ ] Add logging for registration events
- [ ] Write Javadoc for all public methods
- [ ] Add inline comments for password hashing logic
- [ ] Update API documentation with examples

### Acceptance Criteria
- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] Property tests pass (100+ iterations)
- [ ] Code coverage > 80%
- [ ] User can register successfully
- [ ] Duplicate usernames are rejected
- [ ] Inline documentation complete
- [ ] OpenAPI docs updated
- [ ] No linter warnings
- [ ] Code review approved
- [ ] CI/CD pipeline green

### Dependencies
- Requires: Issue 1.2 (TestContainers setup)
- Blocks: Issue 2.2 (Authentication service)
```

---

## Ejemplos Prácticos

### Ejemplo 1: Feature Completa

```bash
# Descripción
"Sistema de gestión de proyectos con usuarios, workspaces, roles y proposals"

# 1. Generar plan
/plan-tdd "Sistema de gestión de proyectos con usuarios, workspaces, roles y proposals"

# 2. Guardar plan
/save-output project-mgmt-plan.md

# 3. Convertir a PRD
node tdd-to-prd.js project-mgmt-plan.md project-mgmt

# 4. Revisar PRD generado
cat prds/project-mgmt.md

# 5. Generar issues
/pm:prd-new project-mgmt
/pm:prd-parse project-mgmt
/pm:epic-oneshot project-mgmt

# Output en GitHub:
# Issue #1: TestContainers setup
# Issue #2: Property testing framework setup
# Issue #3: User Entity and Repository
# Issue #4: UserService with Registration
# [...]
```

### Ejemplo 2: Solo un Milestone

```bash
# Generar plan para un milestone específico
/plan-tdd "Milestone 2: User authentication with JWT and refresh tokens"

# Convertir
node tdd-to-prd.js user-auth-milestone.md auth-milestone

# Generar issues
/pm:prd-new auth-milestone
/pm:prd-parse auth-milestone
/pm:epic-oneshot auth-milestone
```

### Ejemplo 3: Agregar Property Tests a Issue Existente

Si ya tienes issues pero necesitas agregar property tests:

```bash
# 1. Editar issue en GitHub manualmente
# 2. Agregar sección:

**Property Tests** (nuevo):
- [ ] Property 17: User registration → authentication succeeds
  - [ ] Configure jqwik/Hypothesis
  - [ ] Create user data generator
  - [ ] Run 100+ iterations
  - [ ] Validate: every registered user can authenticate

# 3. Actualizar acceptance criteria:
- [ ] Property tests pass (100+ iterations)
```

---

## Verificación de Calidad

### Checklist por Issue

Antes de cerrar un issue, verificar:

```bash
# Tests
✅ All unit tests pass
✅ All integration tests pass
✅ Property tests pass (100+ iterations si aplica)
✅ Code coverage > 80%

# Código
✅ No linter warnings
✅ No compiler warnings
✅ Code review approved

# Documentación
✅ Javadoc/JSDoc en todos los métodos públicos
✅ Inline comments en lógica compleja
✅ OpenAPI docs actualizados (si hay endpoints)

# CI/CD
✅ Pipeline green
✅ No regressions
```

### Checklist por Milestone

Antes de cerrar un milestone:

```bash
✅ Todos los issues completados
✅ Architecture documentation creada
✅ README actualizado con nuevas features
✅ Integration tests entre componentes pasan
✅ Demo funcional del milestone
```

---

## Troubleshooting

### El plan generado no incluye property tests

**Problema**: El agente no detecta propiedades del design doc.

**Solución**:
```bash
# Opción 1: Mencionar propiedades explícitamente
/plan-tdd "Auth system. Properties: 1) Registration → Login succeeds, 2) Password hashing irreversible"

# Opción 2: Proveer design doc
/plan-tdd --with-design design.md "Auth system"
```

### El PRD generado tiene formato incorrecto

**Problema**: El script `tdd-to-prd.js` no parsea bien el plan.

**Solución**:
```bash
# Verificar que el plan sigue el formato esperado
# El plan debe tener:
### Milestone N: Nombre (duración)
**Goal**: ...
**Issues**:
- Issue N.M: Título (estimación)
  - Red: ...
  - Green: ...
  - Refactor: ...

# Si el formato es diferente, editar el plan manualmente o ajustar el script
```

### CCPM no crea los issues

**Problema**: `/pm:epic-oneshot` falla.

**Solución**:
```bash
# 1. Verificar token de GitHub
echo $GITHUB_TOKEN

# 2. Verificar que el PRD está en prds/
ls prds/

# 3. Verificar formato del PRD
cat prds/your-prd.md

# 4. Crear issues manualmente si persiste el error
# Usar el template de Opción C
```

### Los issues no tienen la estructura TDD

**Problema**: Issues creados sin Red-Green-Refactor.

**Solución**:
```bash
# Verificar que el PRD contiene:
**TDD Approach**:

Red Phase:
- [ ] ...

Green Phase:
- [ ] ...

Refactor Phase:
- [ ] ...

# Si falta, editar el PRD y regenerar issues
```

---

## Mejoras Futuras

### Hook Automático

Crear un hook que valide issues antes de cerrarlos:

```json
{
  "matcher": "tool == \"Git\" && tool_input.command == \"commit\"",
  "hooks": [{
    "type": "command",
    "command": "#!/bin/bash\nif ! grep -q '✅ All tests pass' .github/ISSUE_TEMPLATE.md; then\n  echo '[Hook] Missing test checklist' >&2\n  exit 1\nfi"
  }]
}
```

### GitHub Action

Crear action que valide PRs contra acceptance criteria:

```yaml
name: TDD Quality Gate
on: [pull_request]
jobs:
  validate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Check coverage
        run: |
          coverage=$(pytest --cov | grep TOTAL | awk '{print $4}' | sed 's/%//')
          if [ $coverage -lt 80 ]; then
            echo "Coverage $coverage% < 80%"
            exit 1
          fi
      - name: Check property tests
        run: |
          if grep -q "Property [0-9]" README.md; then
            # Verificar que existen property tests
            find . -name "*PropertyTest*"
          fi
```

---

## Recursos Adicionales

- **TDD Skill Original**: `/path/to/tdd-milestone-planning.md`
- **Planner-TDD Agent**: `~/.claude/agents/planner-tdd.md`
- **CCPM Docs**: https://automaze.io/ccpm/docs
- **Property Testing**:
  - jqwik (Java): https://jqwik.net/
  - Hypothesis (Python): https://hypothesis.readthedocs.io/
  - fast-check (JS): https://github.com/dubzzz/fast-check

---

## Conclusión

Con esta integración tienes:

✅ **Plan TDD automático**: Agente especializado genera plan con Red-Green-Refactor
✅ **Property tests integrados**: Mapea propiedades del design a property tests
✅ **Issues en GitHub**: CCPM crea issues automáticamente
✅ **Documentación continua**: Parte de cada tarea, no fase separada
✅ **Quality gates**: Acceptance criteria claros y verificables
✅ **Trazabilidad**: Plan → PRD → Issue → Code

¿Preguntas? ¿Necesitas ayuda con algún paso específico?