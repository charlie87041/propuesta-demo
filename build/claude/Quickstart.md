# 🚀 Quick Start - Claude Code Configuration

## Opción 1: Generador Automático (Recomendado)

### Para tu proyecto Spring Boot:

```bash
python3 generate_claude_config.py \
  --project-name "collaborative-document-platform" \
  --language java \
  --framework spring-boot \
  --build-tool gradle
```

Esto creará toda la estructura en `.claude/` con:
- ✅ `claude_config.json` - Configuración principal
- ✅ `CLAUDE.md` - Documentación del proyecto
- ✅ `package-manager.json` - Gestor de paquetes
- ✅ Agentes base (planner, code-reviewer, etc.)
- ✅ Comandos (/plan, /tdd, etc.)
- ✅ Skills para Spring Boot
- ✅ Rules de coding
- ✅ Hooks y schemas

---

## Opción 2: Manual (Más Control)

### Paso 1: Crear estructura

```bash
mkdir -p .claude/{agents,commands,contexts,skills,rules/common,hooks,schemas,scripts}
```

### Paso 2: Copiar archivos base

```bash
# Copiar configuración
cp claude_config.json .claude/

# Copiar schema
cp config.schema.json .claude/schemas/
```

### Paso 3: Personalizar

Edita `.claude/claude_config.json` según tu proyecto:

```json
{
  "version": "1.0.0",
  "project": {
    "name": "tu-proyecto",
    "language": "java",
    "framework": "spring-boot",
    "buildTool": "gradle"
  },
  "skills": {
    "enabled": [
      "springboot-patterns",
      "springboot-security",
      "springboot-tdd",
      "jpa-patterns"
    ]
  }
}
```

### Paso 4: Crear CLAUDE.md

```bash
cat > .claude/CLAUDE.md << 'INNER_EOF'
## Project Overview

Tu proyecto aquí...

## Critical Rules

- Regla 1
- Regla 2

## Available Commands

- /plan - Crear plan
- /tdd - TDD workflow
INNER_EOF
```

---

## Opción 3: Usar tu configuración existente

Si ya tienes `.claude/` con agents, skills, etc.:

### Agregar solo el claude_config.json:

```bash
cp claude_config.json .claude/
```

### Personalizar para tu proyecto:

```bash
vim .claude/claude_config.json
```

Actualiza:
- `project.name` → "collaborative-document-platform"
- `project.language` → "java"
- `project.framework` → "spring-boot"
- `skills.enabled` → Lista los skills que tienes

---

## Validar tu configuración

Después de generar o modificar:

```bash
# Validar sintaxis JSON
python3 -m json.tool .claude/claude_config.json

# Validar estructura completa
python3 validate_claude_config.py

# Aplicar correcciones automáticas si hay warnings
python3 fix_claude_config.py
```

---

## Ejemplo completo para tu proyecto

```bash
# 1. Generar configuración base
python3 generate_claude_config.py \
  --project-name "collaborative-document-platform" \
  --language java \
  --framework spring-boot \
  --build-tool gradle

# 2. Copiar tus skills existentes (si los tienes)
cp -r /path/to/old/.claude/skills/* .claude/skills/
cp -r /path/to/old/.claude/agents/* .claude/agents/

# 3. Validar
python3 validate_claude_config.py

# 4. Si hay warnings, aplicar correcciones
python3 fix_claude_config.py

# 5. Revisar y personalizar
vim .claude/claude_config.json
vim .claude/CLAUDE.md

# 6. Commitear
git add .claude/
git commit -m "feat: add Claude Code configuration"
```

---

## Estructura final esperada

```
.claude/
├── claude_config.json          ⭐ Archivo principal
├── CLAUDE.md                   ⭐ Documentación
├── package-manager.json        ⭐ Build tool
│
├── agents/                     # Tus 14 agentes actuales
│   ├── planner.md
│   ├── code-reviewer.md
│   └── ...
│
├── skills/                     # Tus 31 skills actuales
│   ├── springboot-patterns/
│   ├── springboot-tdd/
│   └── ...
│
├── commands/                   # Tus 32 comandos actuales
│   ├── plan.md
│   ├── tdd.md
│   └── ...
│
├── contexts/                   # Tus contextos
│   ├── dev.md
│   └── ...
│
├── rules/                      # Tus reglas
│   └── common/
│       ├── coding-style.md
│       └── ...
│
└── schemas/
    └── config.schema.json
```

---

## ¿Qué archivo falta en tu configuración actual?

Según la validación, te falta principalmente:

1. **`claude_config.json`** ← El archivo principal ⭐
2. **`config.schema.json`** ← Schema de validación

Todo lo demás (agents, skills, commands, etc.) ya lo tienes! 🎉

---

## FAQ

### ¿Es obligatorio el claude_config.json?

**Depende de tu versión de Claude Code:**
- Versiones nuevas: Sí, es el punto de entrada
- Versiones viejas: Puede funcionar solo con CLAUDE.md

**Recomendación:** Créalo siempre, es la mejor práctica.

### ¿Puedo usar mi estructura actual sin el config.json?

Sí, pero perderás features como:
- Auto-selección de agentes
- Workflows predefinidos
- Feature flags
- Aliases de comandos

### ¿Qué pasa si tengo ambos CLAUDE.md y claude_config.json?

Perfecto! Se complementan:
- `claude_config.json` → Configuración estructurada
- `CLAUDE.md` → Documentación en prosa

---

## Siguiente paso recomendado

Para tu proyecto actual:

```bash
# 1. Copiar el claude_config.json generado
cp claude_config.json .claude/

# 2. Personalizarlo para tu proyecto
# (Ya está pre-configurado para Spring Boot!)

# 3. Validar que todo funcione
python3 validate_claude_config.py

# ✅ ¡Listo!
```

---

**¿Preguntas?** Revisa `CLAUDE_CONFIG_GUIDE.md` para documentación completa.
