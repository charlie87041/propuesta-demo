# 📘 Guía de Configuración de Claude Code

## Archivos de Configuració# 📘 Guía de Configuración de Claude Code

## Archivos de Configuración Principal

### 1. `claude_config.json` (Obligatorio)

Este es el archivo de configuración principal que Claude Code lee al iniciar.

**Ubicación:** `.claude/claude_config.json` o raíz del proyecto como `claude_config.json`

**Propósito:** Define cómo Claude Code debe comportarse en tu proyecto.

#### Estructura básica:

```json
{
  "version": "1.0.0",
  "project": {
    "name": "mi-proyecto",
    "description": "Descripción del proyecto",
    "language": "java",
    "framework": "spring-boot"
  },
  "agents": {
    "default": "planner",
    "available": ["planner", "code-reviewer", "tdd-guide"],
    "autoSelect": true
  },
  "skills": {
    "enabled": ["backend-patterns", "springboot-tdd"],
    "autoLoad": true
  }
}
```

---

### 2. `CLAUDE.md` (Recomendado)

Archivo de documentación principal del proyecto para Claude.

**Ubicación:** `.claude/CLAUDE.md`

**Propósito:** Proporciona contexto de alto nivel sobre el proyecto.

#### Estructura recomendada:

```markdown
## Project Overview
[Descripción general del proyecto]

## Critical Rules
[Reglas que SIEMPRE deben seguirse]

## File Structure
[Organización de archivos]

## Key Patterns
[Patrones importantes del proyecto]

## Available Commands
[Comandos personalizados]

## Git Workflow
[Flujo de trabajo de Git]
```

---

### 3. `package-manager.json` (Opcional)

Define el gestor de paquetes del proyecto.

**Ubicación:** `.claude/package-manager.json`

```json
{
  "packageManager": "npm",
  "setAt": "2026-02-09T12:00:00.000Z"
}
```

Opciones: `"npm"`, `"yarn"`, `"pnpm"`, `"bun"`, `"gradle"`, `"maven"`

---

## Estructura de Directorios Completa

```
.claude/
├── claude_config.json          # ⭐ Configuración principal
├── CLAUDE.md                   # ⭐ Documentación del proyecto
├── package-manager.json        # Gestor de paquetes
│
├── agents/                     # Agentes especializados
│   ├── planner.md
│   ├── code-reviewer.md
│   └── tdd-guide.md
│
├── commands/                   # Comandos personalizados
│   ├── plan.md
│   ├── tdd.md
│   └── code-review.md
│
├── contexts/                   # Contextos del proyecto
│   ├── dev.md
│   ├── review.md
│   └── research.md
│
├── skills/                     # Skills especializados
│   ├── backend-patterns/
│   │   └── SKILL.md
│   ├── springboot-tdd/
│   │   └── SKILL.md
│   └── ...
│
├── rules/                      # Reglas del proyecto
│   ├── README.md
│   └── common/
│       ├── coding-style.md
│       ├── security.md
│       └── testing.md
│
├── hooks/                      # Hooks de automatización
│   └── hooks.json
│
├── schemas/                    # Schemas de validación
│   ├── config.schema.json
│   ├── hooks.schema.json
│   └── plugin.schema.json
│
└── scripts/                    # Scripts de utilidad
    ├── ci/
    └── hooks/
```

---

## Cómo Generar la Configuración

### Opción 1: Manualmente

1. **Crear el directorio:**
   ```bash
   mkdir -p .claude/{agents,commands,contexts,skills,rules,hooks,schemas,scripts}
   ```

2. **Crear `claude_config.json`:**
   ```bash
   cat > .claude/claude_config.json << 'EOF'
   {
     "version": "1.0.0",
     "project": {
       "name": "tu-proyecto",
       "language": "java"
     }
   }
   EOF
   ```

3. **Crear `CLAUDE.md`:**
   ```bash
   cat > .claude/CLAUDE.md << 'EOF'
   ## Project Overview
   
   [Describe tu proyecto aquí]
   
   ## Critical Rules
   
   - Regla 1
   - Regla 2
   EOF
   ```

### Opción 2: Usando el generador

Puedes usar el script que te proporcioné:

```bash
python3 generate_claude_config.py --project-name "mi-proyecto" \
  --language java \
  --framework spring-boot
```

### Opción 3: Copiar plantilla existente

```bash
# Copiar configuración de ejemplo
cp -r /path/to/example/.claude .claude

# Personalizar
vim .claude/claude_config.json
vim .claude/CLAUDE.md
```

---

## Configuración por Tipo de Proyecto

### Spring Boot (Java)

```json
{
  "project": {
    "language": "java",
    "framework": "spring-boot",
    "buildTool": "gradle"
  },
  "skills": {
    "enabled": [
      "springboot-patterns",
      "springboot-security",
      "springboot-tdd",
      "jpa-patterns",
      "java-coding-standards"
    ]
  }
}
```

### Django (Python)

```json
{
  "project": {
    "language": "python",
    "framework": "django",
    "buildTool": "pip"
  },
  "skills": {
    "enabled": [
      "django-patterns",
      "django-security",
      "django-tdd",
      "python-patterns",
      "python-testing"
    ]
  }
}
```

### React (TypeScript)

```json
{
  "project": {
    "language": "typescript",
    "framework": "react",
    "buildTool": "npm"
  },
  "skills": {
    "enabled": [
      "frontend-patterns",
      "coding-standards"
    ]
  }
}
```

### Go

```json
{
  "project": {
    "language": "go",
    "buildTool": "go"
  },
  "skills": {
    "enabled": [
      "golang-patterns",
      "golang-testing"
    ]
  }
}
```

---

## Secciones Importantes del Config

### 1. Agents

Define qué agentes están disponibles y cuál usar por defecto:

```json
{
  "agents": {
    "default": "planner",           // Agente por defecto
    "available": [                  // Agentes disponibles
      "planner",
      "code-reviewer",
      "tdd-guide"
    ],
    "autoSelect": true              // Auto-seleccionar agente apropiado
  }
}
```

### 2. Skills

Habilita skills específicos para tu proyecto:

```json
{
  "skills": {
    "enabled": [
      "backend-patterns",           // Patrones de backend
      "springboot-tdd",             // TDD para Spring Boot
      "security-review"             // Revisión de seguridad
    ],
    "autoLoad": true                // Cargar automáticamente
  }
}
```

### 3. Workflows

Define flujos de trabajo predefinidos:

```json
{
  "workflows": {
    "development": {
      "steps": ["plan-tdd", "tdd", "code-review", "verify"]
    },
    "refactoring": {
      "steps": ["code-review", "refactor-clean", "verify"]
    }
  }
}
```

Uso:
```bash
/workflow development
```

### 4. Command Aliases

Crea atajos para comandos:

```json
{
  "commands": {
    "aliases": {
      "t": "tdd",           // /t en lugar de /tdd
      "p": "plan",          // /p en lugar de /plan
      "r": "code-review"    // /r en lugar de /code-review
    }
  }
}
```

### 5. Features

Habilita/deshabilita características:

```json
{
  "features": {
    "continuousLearning": true,     // Aprendizaje continuo
    "strategicCompact": true,       // Compactación inteligente
    "autoDocumentation": true,      // Auto-documentación
    "testGeneration": true          // Generación de tests
  }
}
```

---

## Validación de Configuración

### Validar sintaxis JSON:

```bash
# Con jq
jq empty .claude/claude_config.json

# Con Python
python3 -m json.tool .claude/claude_config.json
```

### Validar contra schema:

```bash
# Con ajv-cli
npm install -g ajv-cli
ajv validate -s .claude/schemas/config.schema.json \
  -d .claude/claude_config.json
```

### Validar estructura completa:

```bash
python3 validate_claude_config.py
```

---

## Mejores Prácticas

### 1. Versionado

- Incluye `claude_config.json` en tu repositorio
- Versiona cambios importantes
- Documenta cambios en CHANGELOG

### 2. Organización

- Un skill por responsabilidad
- Agrupa rules por categoría
- Nombres descriptivos en kebab-case

### 3. Documentación

- Mantén `CLAUDE.md` actualizado
- Documenta cada skill en su `SKILL.md`
- Incluye ejemplos en todos los archivos

### 4. Seguridad

- NO incluyas secrets en la configuración
- Usa variables de entorno
- Revisa permisos de archivos

### 5. Mantenimiento

- Revisa configuración regularmente
- Elimina skills no usados
- Actualiza documentación al cambiar código

---

## Troubleshooting

### Claude Code no lee mi configuración

**Solución:**
1. Verifica que el archivo esté en `.claude/claude_config.json`
2. Valida sintaxis JSON
3. Revisa permisos del archivo

### Skills no se cargan

**Solución:**
1. Verifica que estén en la lista `skills.enabled`
2. Confirma que el directorio `skills/` existe
3. Revisa que cada skill tenga su `SKILL.md`

### Comandos no funcionan

**Solución:**
1. Verifica que los archivos `.md` existan en `commands/`
2. Confirma el nombre del archivo (sin `/` al inicio)
3. Revisa sintaxis del comando

---

## Recursos Adicionales

- **Ejemplos:** Ver `/examples` en este repositorio
- **Plantillas:** Ver `/templates` para plantillas predefinidas
- **Documentación oficial:** https://docs.claude.com (si existe)

---

## Checklist de Configuración

- [ ] ✅ Directorio `.claude/` creado
- [ ] ✅ `claude_config.json` creado y validado
- [ ] ✅ `CLAUDE.md` documentado
- [ ] ✅ Agents necesarios agregados
- [ ] ✅ Skills relevantes habilitados
- [ ] ✅ Comandos personalizados creados
- [ ] ✅ Rules del proyecto definidas
- [ ] ✅ Workflows configurados
- [ ] ✅ Validación ejecutada sin errores
- [ ] ✅ Configuración commiteada al repo

---

**¡Tu configuración de Claude Code está lista! 🚀**n Principal

### 1. `claude_config.json` (Obligatorio)

Este es el archivo de configuración principal que Claude Code lee al iniciar.

**Ubicación:** `.claude/claude_config.json` o raíz del proyecto como `claude_config.json`

**Propósito:** Define cómo Claude Code debe comportarse en tu proyecto.

#### Estructura básica:

```json
{
  "version": "1.0.0",
  "project": {
    "name": "mi-proyecto",
    "description": "Descripción del proyecto",
    "language": "java",
    "framework": "spring-boot"
  },
  "agents": {
    "default": "planner",
    "available": ["planner", "code-reviewer", "tdd-guide"],
    "autoSelect": true
  },
  "skills": {
    "enabled": ["backend-patterns", "springboot-tdd"],
    "autoLoad": true
  }
}
```

---

### 2. `CLAUDE.md` (Recomendado)

Archivo de documentación principal del proyecto para Claude.

**Ubicación:** `.claude/CLAUDE.md`

**Propósito:** Proporciona contexto de alto nivel sobre el proyecto.

#### Estructura recomendada:

```markdown
## Project Overview
[Descripción general del proyecto]

## Critical Rules
[Reglas que SIEMPRE deben seguirse]

## File Structure
[Organización de archivos]

## Key Patterns
[Patrones importantes del proyecto]

## Available Commands
[Comandos personalizados]

## Git Workflow
[Flujo de trabajo de Git]
```

---

### 3. `package-manager.json` (Opcional)

Define el gestor de paquetes del proyecto.

**Ubicación:** `.claude/package-manager.json`

```json
{
  "packageManager": "npm",
  "setAt": "2026-02-09T12:00:00.000Z"
}
```

Opciones: `"npm"`, `"yarn"`, `"pnpm"`, `"bun"`, `"gradle"`, `"maven"`

---

## Estructura de Directorios Completa

```
.claude/
├── claude_config.json          # ⭐ Configuración principal
├── CLAUDE.md                   # ⭐ Documentación del proyecto
├── package-manager.json        # Gestor de paquetes
│
├── agents/                     # Agentes especializados
│   ├── planner.md
│   ├── code-reviewer.md
│   └── tdd-guide.md
│
├── commands/                   # Comandos personalizados
│   ├── plan.md
│   ├── tdd.md
│   └── code-review.md
│
├── contexts/                   # Contextos del proyecto
│   ├── dev.md
│   ├── review.md
│   └── research.md
│
├── skills/                     # Skills especializados
│   ├── backend-patterns/
│   │   └── SKILL.md
│   ├── springboot-tdd/
│   │   └── SKILL.md
│   └── ...
│
├── rules/                      # Reglas del proyecto
│   ├── README.md
│   └── common/
│       ├── coding-style.md
│       ├── security.md
│       └── testing.md
│
├── hooks/                      # Hooks de automatización
│   └── hooks.json
│
├── schemas/                    # Schemas de validación
│   ├── config.schema.json
│   ├── hooks.schema.json
│   └── plugin.schema.json
│
└── scripts/                    # Scripts de utilidad
    ├── ci/
    └── hooks/
```

---

## Cómo Generar la Configuración

### Opción 1: Manualmente

1. **Crear el directorio:**
   ```bash
   mkdir -p .claude/{agents,commands,contexts,skills,rules,hooks,schemas,scripts}
   ```

2. **Crear `claude_config.json`:**
   ```bash
   cat > .claude/claude_config.json << 'EOF'
   {
     "version": "1.0.0",
     "project": {
       "name": "tu-proyecto",
       "language": "java"
     }
   }
   EOF
   ```

3. **Crear `CLAUDE.md`:**
   ```bash
   cat > .claude/CLAUDE.md << 'EOF'
   ## Project Overview
   
   [Describe tu proyecto aquí]
   
   ## Critical Rules
   
   - Regla 1
   - Regla 2
   EOF
   ```

### Opción 2: Usando el generador

Puedes usar el script que te proporcioné:

```bash
python3 generate_claude_config.py --project-name "mi-proyecto" \
  --language java \
  --framework spring-boot
```

### Opción 3: Copiar plantilla existente

```bash
# Copiar configuración de ejemplo
cp -r /path/to/example/.claude .claude

# Personalizar
vim .claude/claude_config.json
vim .claude/CLAUDE.md
```

---

## Configuración por Tipo de Proyecto

### Spring Boot (Java)

```json
{
  "project": {
    "language": "java",
    "framework": "spring-boot",
    "buildTool": "gradle"
  },
  "skills": {
    "enabled": [
      "springboot-patterns",
      "springboot-security",
      "springboot-tdd",
      "jpa-patterns",
      "java-coding-standards"
    ]
  }
}
```

### Django (Python)

```json
{
  "project": {
    "language": "python",
    "framework": "django",
    "buildTool": "pip"
  },
  "skills": {
    "enabled": [
      "django-patterns",
      "django-security",
      "django-tdd",
      "python-patterns",
      "python-testing"
    ]
  }
}
```

### React (TypeScript)

```json
{
  "project": {
    "language": "typescript",
    "framework": "react",
    "buildTool": "npm"
  },
  "skills": {
    "enabled": [
      "frontend-patterns",
      "coding-standards"
    ]
  }
}
```

### Go

```json
{
  "project": {
    "language": "go",
    "buildTool": "go"
  },
  "skills": {
    "enabled": [
      "golang-patterns",
      "golang-testing"
    ]
  }
}
```

---

## Secciones Importantes del Config

### 1. Agents

Define qué agentes están disponibles y cuál usar por defecto:

```json
{
  "agents": {
    "default": "planner",           // Agente por defecto
    "available": [                  // Agentes disponibles
      "planner",
      "code-reviewer",
      "tdd-guide"
    ],
    "autoSelect": true              // Auto-seleccionar agente apropiado
  }
}
```

### 2. Skills

Habilita skills específicos para tu proyecto:

```json
{
  "skills": {
    "enabled": [
      "backend-patterns",           // Patrones de backend
      "springboot-tdd",             // TDD para Spring Boot
      "security-review"             // Revisión de seguridad
    ],
    "autoLoad": true                // Cargar automáticamente
  }
}
```

### 3. Workflows

Define flujos de trabajo predefinidos:

```json
{
  "workflows": {
    "development": {
      "steps": ["plan-tdd", "tdd", "code-review", "verify"]
    },
    "refactoring": {
      "steps": ["code-review", "refactor-clean", "verify"]
    }
  }
}
```

Uso:
```bash
/workflow development
```

### 4. Command Aliases

Crea atajos para comandos:

```json
{
  "commands": {
    "aliases": {
      "t": "tdd",           // /t en lugar de /tdd
      "p": "plan",          // /p en lugar de /plan
      "r": "code-review"    // /r en lugar de /code-review
    }
  }
}
```

### 5. Features

Habilita/deshabilita características:

```json
{
  "features": {
    "continuousLearning": true,     // Aprendizaje continuo
    "strategicCompact": true,       // Compactación inteligente
    "autoDocumentation": true,      // Auto-documentación
    "testGeneration": true          // Generación de tests
  }
}
```

---

## Validación de Configuración

### Validar sintaxis JSON:

```bash
# Con jq
jq empty .claude/claude_config.json

# Con Python
python3 -m json.tool .claude/claude_config.json
```

### Validar contra schema:

```bash
# Con ajv-cli
npm install -g ajv-cli
ajv validate -s .claude/schemas/config.schema.json \
  -d .claude/claude_config.json
```

### Validar estructura completa:

```bash
python3 validate_claude_config.py
```

---

## Mejores Prácticas

### 1. Versionado

- Incluye `claude_config.json` en tu repositorio
- Versiona cambios importantes
- Documenta cambios en CHANGELOG

### 2. Organización

- Un skill por responsabilidad
- Agrupa rules por categoría
- Nombres descriptivos en kebab-case

### 3. Documentación

- Mantén `CLAUDE.md` actualizado
- Documenta cada skill en su `SKILL.md`
- Incluye ejemplos en todos los archivos

### 4. Seguridad

- NO incluyas secrets en la configuración
- Usa variables de entorno
- Revisa permisos de archivos

### 5. Mantenimiento

- Revisa configuración regularmente
- Elimina skills no usados
- Actualiza documentación al cambiar código

---

## Troubleshooting

### Claude Code no lee mi configuración

**Solución:**
1. Verifica que el archivo esté en `.claude/claude_config.json`
2. Valida sintaxis JSON
3. Revisa permisos del archivo

### Skills no se cargan

**Solución:**
1. Verifica que estén en la lista `skills.enabled`
2. Confirma que el directorio `skills/` existe
3. Revisa que cada skill tenga su `SKILL.md`

### Comandos no funcionan

**Solución:**
1. Verifica que los archivos `.md` existan en `commands/`
2. Confirma el nombre del archivo (sin `/` al inicio)
3. Revisa sintaxis del comando

---

## Recursos Adicionales

- **Ejemplos:** Ver `/examples` en este repositorio
- **Plantillas:** Ver `/templates` para plantillas predefinidas
- **Documentación oficial:** https://docs.claude.com (si existe)

---

## Checklist de Configuración

- [ ] ✅ Directorio `.claude/` creado
- [ ] ✅ `claude_config.json` creado y validado
- [ ] ✅ `CLAUDE.md` documentado
- [ ] ✅ Agents necesarios agregados
- [ ] ✅ Skills relevantes habilitados
- [ ] ✅ Comandos personalizados creados
- [ ] ✅ Rules del proyecto definidas
- [ ] ✅ Workflows configurados
- [ ] ✅ Validación ejecutada sin errores
- [ ] ✅ Configuración commiteada al repo

---

**¡Tu configuración de Claude Code está lista! 🚀**
