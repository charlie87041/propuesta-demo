# Claude Code to Kiro Bridge

Esta guía explica cómo adaptar configuraciones de Claude Code (como everything-claude-code) para usar en Kiro.

## Mapeo de Conceptos

| Claude Code | Kiro | Notas |
|-------------|------|-------|
| `~/.claude/agents/` | Subagents via `invokeSubAgent` | Kiro usa subagents integrados |
| `~/.claude/rules/` | `.kiro/steering/` | Steering files con frontmatter |
| `~/.claude/skills/` | `.kiro/skills/` | ✅ Compatible directo |
| `~/.claude/commands/` | Skills + Hooks | Simular con skills activables |
| `~/.claude.json` (MCP) | `.kiro/settings/mcp.json` | ✅ Compatible MCP |
| Hooks (PreToolUse, etc.) | `.kiro/hooks/` | Sintaxis ligeramente diferente |

## Adaptación de Componentes

### 1. Skills ✅ Directo

Los skills de Claude Code funcionan directamente en Kiro:

```bash
# Copiar skills de everything-claude-code
cp -r everything-claude-code/skills/* .kiro/skills/
```

**Estructura de skill compatible:**
```markdown
---
name: tdd-workflow
description: Test-driven development workflow
inclusion: manual
---

# TDD Workflow

1. Define interfaces first
2. Write failing tests (RED)
3. Implement minimal code (GREEN)
4. Refactor (IMPROVE)
```

### 2. Rules → Steering

Las "rules" de Claude se convierten en "steering files" en Kiro:

```bash
# Crear directorio steering
mkdir -p .kiro/steering

# Adaptar rules a steering
cp everything-claude-code/rules/common/coding-style.md .kiro/steering/
cp everything-claude-code/rules/common/testing.md .kiro/steering/
```

**Agregar frontmatter para control de inclusión:**
```markdown
---
inclusion: auto
---

# Coding Standards

[contenido de la rule...]
```

Opciones de `inclusion`:
- `auto` - Siempre incluido (default)
- `manual` - Solo cuando se invoca con `#`
- `fileMatch` - Cuando se lee un archivo que coincide con el patrón

### 3. Commands → Skills Invocables

Los comandos slash de Claude (`/tdd`, `/plan`) se simulan con skills:

**Ejemplo: Convertir `/tdd` command a skill**

```markdown
---
name: tdd-command
description: Run TDD workflow on current task
inclusion: manual
---

# TDD Command

When invoked, follow this workflow:

1. **Analyze current task** from context
2. **Define interfaces** before implementation
3. **Write failing test** (RED phase)
4. **Implement minimal code** (GREEN phase)
5. **Refactor** (IMPROVE phase)
6. **Verify coverage** (80%+ required)

## Usage

User invokes with: "Run TDD workflow" or "#tdd-command"
```

### 4. Agents → Subagents

Claude agents se mapean a los subagents de Kiro:

**Subagents disponibles en Kiro:**
- `general-task-execution` - Tareas generales
- `context-gatherer` - Análisis de repositorio
- `spec-task-execution` - Ejecución de tareas de specs
- `feature-requirements-first-workflow` - Workflow de specs

**Ejemplo de uso:**
```markdown
Para delegar una tarea de code review, usa:
invokeSubAgent(
  name: "general-task-execution",
  prompt: "Review the code in src/components for security issues",
  explanation: "Delegating security review"
)
```

### 5. Hooks

**Claude Code hooks.json:**
```json
{
  "matcher": "tool == \"Edit\"",
  "hooks": [{
    "type": "command",
    "command": "echo 'File edited'"
  }]
}
```

**Kiro hooks (usando createHook tool):**
```javascript
createHook({
  id: "lint-on-save",
  name: "Lint on Save",
  description: "Run linter when TypeScript files are edited",
  eventType: "fileEdited",
  filePatterns: "*.ts,*.tsx",
  hookAction: "askAgent",
  outputPrompt: "Run npm run lint and fix any errors",
  why: "Ensure code quality on save"
})
```

**Eventos disponibles en Kiro:**
- `fileEdited`, `fileCreated`, `fileDeleted`
- `promptSubmit`, `agentStop`
- `preToolUse`, `postToolUse`
- `userTriggered`

### 6. MCP Servers ✅ Compatible

Los MCP servers son compatibles:

```bash
# Copiar configuración MCP
cp everything-claude-code/mcp-configs/mcp-servers.json .kiro/settings/mcp.json
```

**Estructura compatible:**
```json
{
  "mcpServers": {
    "github": {
      "command": "uvx",
      "args": ["mcp-server-github"],
      "env": {
        "GITHUB_TOKEN": "YOUR_TOKEN_HERE"
      }
    }
  }
}
```

## Workflow de Adaptación Completo

### Paso 1: Clonar everything-claude-code
```bash
git clone https://github.com/affaan-m/everything-claude-code.git
cd everything-claude-code
```

### Paso 2: Copiar Skills
```bash
# Copiar todos los skills
cp -r skills/* ../.kiro/skills/

# O copiar selectivamente
cp skills/tdd-workflow/*.md ../.kiro/skills/
cp skills/security-review/*.md ../.kiro/skills/
```

### Paso 3: Adaptar Rules a Steering
```bash
mkdir -p ../.kiro/steering

# Copiar rules comunes
for file in rules/common/*.md; do
  echo "---" > "../.kiro/steering/$(basename $file)"
  echo "inclusion: auto" >> "../.kiro/steering/$(basename $file)"
  echo "---" >> "../.kiro/steering/$(basename $file)"
  echo "" >> "../.kiro/steering/$(basename $file)"
  cat "$file" >> "../.kiro/steering/$(basename $file)"
done

# Copiar rules específicas de lenguaje (ej: TypeScript)
for file in rules/typescript/*.md; do
  echo "---" > "../.kiro/steering/ts-$(basename $file)"
  echo "inclusion: fileMatch" >> "../.kiro/steering/ts-$(basename $file)"
  echo "fileMatchPattern: '**/*.ts'" >> "../.kiro/steering/ts-$(basename $file)"
  echo "---" >> "../.kiro/steering/ts-$(basename $file)"
  echo "" >> "../.kiro/steering/ts-$(basename $file)"
  cat "$file" >> "../.kiro/steering/ts-$(basename $file)"
done
```

### Paso 4: Crear Skills para Comandos
```bash
# Crear skills invocables que simulan comandos
mkdir -p ../.kiro/skills/commands
```

Luego crear archivos como:
- `.kiro/skills/commands/plan.md` (simula `/plan`)
- `.kiro/skills/commands/tdd.md` (simula `/tdd`)
- `.kiro/skills/commands/code-review.md` (simula `/code-review`)

### Paso 5: Configurar Hooks
Usar el comando de Kiro para crear hooks:
```
"Create a hook that runs linter on TypeScript file edits"
```

O usar la UI de hooks en Kiro.

### Paso 6: Configurar MCP (opcional)
```bash
mkdir -p ../.kiro/settings
cp mcp-configs/mcp-servers.json ../.kiro/settings/mcp.json
# Editar y agregar tus API keys
```

## Diferencias Clave a Considerar

### 1. No hay comandos slash en Kiro
**Solución:** Invocar skills manualmente con `#skill-name` o crear hooks que los activen

### 2. Sistema de plugins diferente
**Solución:** Copiar componentes directamente en lugar de instalar como plugin

### 3. Sintaxis de hooks diferente
**Solución:** Usar `createHook` tool o la UI de hooks de Kiro

### 4. Agents vs Subagents
**Solución:** Usar los subagents integrados de Kiro o crear steering files que guíen el comportamiento

## Ejemplos Prácticos

### Ejemplo 1: Usar TDD Workflow

**En Claude Code:**
```
/tdd "implement user authentication"
```

**En Kiro:**
```
#tdd-workflow

Implement user authentication following TDD principles
```

### Ejemplo 2: Code Review

**En Claude Code:**
```
/code-review src/auth/
```

**En Kiro:**
```
Review the code in src/auth/ for:
- Security issues
- Code quality
- Best practices
- Test coverage

Use #security-review and #coding-standards for guidance
```

### Ejemplo 3: Continuous Learning

**En Claude Code:**
```
/learn
```

**En Kiro:**
```
Analyze the recent changes in this session and extract:
- Patterns I'm following
- Common mistakes
- Useful techniques

Create a new skill in .kiro/skills/ to capture this knowledge
```

## Recursos

- [Everything Claude Code Repo](https://github.com/affaan-m/everything-claude-code)
- [Kiro Skills Documentation](../.kiro/skills/README.md)
- [Kiro Hooks Documentation](https://docs.kiro.ai/hooks)
- [MCP Protocol](https://modelcontextprotocol.io)

## Contribuir

Si encuentras mejores formas de adaptar componentes de Claude Code a Kiro, por favor documéntalas aquí.
