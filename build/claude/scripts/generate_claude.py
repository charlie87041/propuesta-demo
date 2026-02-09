#!/usr/bin/env python3
"""
Generador de configuración de Claude Code
Crea estructura completa de .claude/ con archivos base
"""

import json
import argparse
from pathlib import Path
from datetime import datetime

class ClaudeConfigGenerator:
    def __init__(self, output_dir: str = ".claude"):
        self.output_dir = Path(output_dir)
        
    def generate(self, project_name: str, language: str, framework: str = None, 
                 build_tool: str = None):
        """Genera configuración completa"""
        print(f"🚀 Generando configuración de Claude Code para '{project_name}'...\n")
        
        # Crear estructura de directorios
        self._create_directory_structure()
        
        # Generar archivos de configuración
        self._generate_config_json(project_name, language, framework, build_tool)
        self._generate_claude_md(project_name, language, framework)
        self._generate_package_manager_json(build_tool or self._infer_build_tool(language))
        
        # Generar archivos base
        self._generate_base_agent()
        self._generate_base_command()
        self._generate_base_skill()
        self._generate_base_context()
        self._generate_base_rule()
        
        # Generar schemas
        self._copy_schemas()
        
        # Generar hooks
        self._generate_hooks()
        
        print("\n✅ Configuración generada exitosamente!")
        print(f"\n📁 Archivos creados en: {self.output_dir}/")
        self._print_next_steps()
    
    def _create_directory_structure(self):
        """Crea estructura de directorios"""
        dirs = [
            'agents',
            'commands',
            'contexts',
            'skills',
            'rules/common',
            'hooks',
            'schemas',
            'scripts/ci',
            'scripts/hooks'
        ]
        
        for dir_name in dirs:
            dir_path = self.output_dir / dir_name
            dir_path.mkdir(parents=True, exist_ok=True)
            print(f"  📁 {dir_path}")
    
    def _generate_config_json(self, project_name: str, language: str, 
                             framework: str, build_tool: str):
        """Genera claude_config.json"""
        
        # Determinar skills según lenguaje/framework
        skills = self._get_recommended_skills(language, framework)
        
        # Determinar agentes recomendados
        agents = self._get_recommended_agents(language, framework)
        
        config = {
            "$schema": "./schemas/config.schema.json",
            "version": "1.0.0",
            "project": {
                "name": project_name,
                "description": f"Proyecto {project_name}",
                "language": language
            },
            "agents": {
                "default": "planner",
                "available": agents,
                "autoSelect": True
            },
            "skills": {
                "enabled": skills,
                "autoLoad": True
            },
            "contexts": {
                "available": ["dev", "review", "research"],
                "default": "dev"
            },
            "rules": {
                "enabled": [
                    "common/coding-style",
                    "common/security",
                    "common/testing"
                ],
                "strict": True
            },
            "commands": {
                "aliases": {
                    "t": "tdd",
                    "p": "plan",
                    "r": "code-review"
                }
            },
            "workflows": {
                "development": {
                    "steps": ["plan", "tdd", "code-review", "verify"]
                }
            },
            "integrations": {
                "hooks": {
                    "enabled": True,
                    "configPath": "hooks/hooks.json"
                }
            },
            "features": {
                "continuousLearning": True,
                "autoDocumentation": True,
                "codeGeneration": True,
                "testGeneration": True
            },
            "preferences": {
                "verbosity": "normal",
                "formatOnSave": True,
                "lintOnSave": True
            }
        }
        
        if framework:
            config["project"]["framework"] = framework
        if build_tool:
            config["project"]["buildTool"] = build_tool
        
        config_file = self.output_dir / "claude_config.json"
        with open(config_file, 'w') as f:
            json.dump(config, f, indent=2)
        
        print(f"  ✅ {config_file}")
    
    def _generate_claude_md(self, project_name: str, language: str, framework: str):
        """Genera CLAUDE.md"""
        
        framework_text = f" con {framework}" if framework else ""
        
        content = f"""
## Project Overview

Proyecto **{project_name}** desarrollado en {language}{framework_text}.

## Critical Rules

### 1. Code Organization

- Código organizado y modular
- Alta cohesión, bajo acoplamiento
- Archivos pequeños y enfocados (200-400 líneas típico, 800 máx)

### 2. Code Style

- Sin emojis en código o comentarios
- Inmutabilidad preferida
- Manejo apropiado de errores
- Validación de inputs

### 3. Testing

- TDD cuando sea apropiado
- Mínimo 80% de cobertura
- Tests unitarios, integración y E2E

### 4. Security

- Sin secrets hardcodeados
- Variables de entorno para datos sensibles
- Validación de todos los inputs
- Queries parametrizadas

## File Structure

```
{project_name}/
├── src/
├── tests/
├── docs/
└── .claude/
```

## Key Patterns

### Error Handling

```{language}
// Manejar errores apropiadamente
try {{
  // código
}} catch (error) {{
  // manejo de error
}}
```

## Available Commands

- `/plan` - Crear plan de implementación
- `/tdd` - Workflow de Test-Driven Development
- `/code-review` - Revisar calidad del código
- `/verify` - Verificar implementación

## Git Workflow

- Conventional commits: `feat:`, `fix:`, `refactor:`, `docs:`, `test:`
- Never commit to main directly
- PRs requieren revisión
- Todos los tests deben pasar antes de merge

## Environment Variables

```bash
# Configurar según tu proyecto
DATABASE_URL=
API_KEY=
DEBUG=false
```

## Notes

Para información más detallada, revisar los archivos en `.claude/`
"""
        
        claude_md_file = self.output_dir / "CLAUDE.md"
        with open(claude_md_file, 'w') as f:
            f.write(content.strip())
        
        print(f"  ✅ {claude_md_file}")
    
    def _generate_package_manager_json(self, build_tool: str):
        """Genera package-manager.json"""
        config = {
            "packageManager": build_tool,
            "setAt": datetime.utcnow().isoformat() + "Z"
        }
        
        pm_file = self.output_dir / "package-manager.json"
        with open(pm_file, 'w') as f:
            json.dump(config, f, indent=2)
        
        print(f"  ✅ {pm_file}")
    
    def _generate_base_agent(self):
        """Genera un agente de ejemplo"""
        content = """# Agent: Planner

## Purpose

Este agente ayuda a crear planes de implementación detallados para nuevas features o refactorizaciones.

## Approach

1. Analiza el requerimiento
2. Descompone en tareas manejables
3. Identifica dependencias
4. Propone orden de implementación
5. Estima complejidad

## Usage

Usa este agente cuando:
- Necesites planificar una nueva feature
- Vayas a hacer un refactor grande
- Requieras estimar trabajo

## Examples

```bash
/plan Implementar autenticación de usuarios
```

## Output

Genera un plan estructurado con:
- Tareas ordenadas
- Dependencias
- Estimaciones
- Consideraciones
"""
        
        agent_file = self.output_dir / "agents" / "planner.md"
        with open(agent_file, 'w') as f:
            f.write(content.strip())
        
        print(f"  ✅ {agent_file}")
    
    def _generate_base_command(self):
        """Genera un comando de ejemplo"""
        content = """# Command: /plan

**Descripción:** Crea un plan de implementación detallado

## Usage

```bash
/plan [descripción de la tarea]
```

## Purpose

Genera un plan estructurado y detallado para implementar una nueva feature o realizar un refactor.

## Examples

### Ejemplo 1: Nueva feature

```bash
/plan Agregar sistema de notificaciones por email
```

### Ejemplo 2: Refactor

```bash
/plan Refactorizar capa de datos para usar repository pattern
```

## Output

El comando genera:
- Lista de tareas en orden lógico
- Dependencias entre tareas
- Estimaciones de complejidad
- Consideraciones técnicas
- Riesgos potenciales
"""
        
        cmd_file = self.output_dir / "commands" / "plan.md"
        with open(cmd_file, 'w') as f:
            f.write(content.strip())
        
        print(f"  ✅ {cmd_file}")
    
    def _generate_base_skill(self):
        """Genera un skill de ejemplo"""
        content = """# Skill: Coding Standards

## Purpose

Asegura que el código siga estándares consistentes de estilo y calidad.

## When to Use

- Al escribir nuevo código
- Durante code reviews
- Al refactorizar código existente

## Core Concepts

- Nombres descriptivos
- Funciones pequeñas y enfocadas
- Comentarios útiles (no obvios)
- Manejo apropiado de errores

## Best Practices

### Naming

- Variables: `camelCase` o `snake_case` según lenguaje
- Funciones: verbos descriptivos
- Clases: sustantivos en PascalCase
- Constantes: UPPER_SNAKE_CASE

### Functions

- Una responsabilidad por función
- Máximo 20-30 líneas idealmente
- Nombres que describen qué hacen
- Parámetros limitados (máx 3-4)

### Comments

```javascript
// ❌ Mal: Comenta lo obvio
let x = 5; // set x to 5

// ✅ Bien: Explica el por qué
let maxRetries = 5; // Retry up to 5 times due to network instability
```

## Common Pitfalls

- ❌ Nombres de variables de una letra (excepto en loops)
- ❌ Funciones gigantes que hacen muchas cosas
- ❌ Magic numbers sin explicación
- ❌ Código comentado que debería eliminarse

## Related Skills

- `testing` - Para escribir tests de calidad
- `security-review` - Para código seguro
"""
        
        skill_dir = self.output_dir / "skills" / "coding-standards"
        skill_dir.mkdir(exist_ok=True)
        
        skill_file = skill_dir / "SKILL.md"
        with open(skill_file, 'w') as f:
            f.write(content.strip())
        
        print(f"  ✅ {skill_file}")
    
    def _generate_base_context(self):
        """Genera un contexto de ejemplo"""
        content = """# Context: Development

Este contexto se usa durante el desarrollo activo de código.

## Guidelines

- Prioriza calidad sobre velocidad
- Escribe tests junto con el código
- Documenta decisiones importantes
- Refactoriza oportunísticamente

## Focus Areas

1. **Correctitud**: El código debe funcionar correctamente
2. **Legibilidad**: Otros desarrolladores deben entenderlo
3. **Mantenibilidad**: Fácil de modificar en el futuro
4. **Performance**: Adecuado para el caso de uso

## Tools Available

- Linters automáticos
- Test runners
- Formatters
- Static analysis

## When to Use

- Implementando nuevas features
- Arreglando bugs
- Refactorizando código existente
"""
        
        ctx_file = self.output_dir / "contexts" / "dev.md"
        with open(ctx_file, 'w') as f:
            f.write(content.strip())
        
        print(f"  ✅ {ctx_file}")
    
    def _generate_base_rule(self):
        """Genera una regla de ejemplo"""
        content = """# Rule: Coding Style

## General Principles

1. **Consistencia**: El código debe verse como si una sola persona lo escribió
2. **Claridad**: Preferir código claro sobre código "clever"
3. **Simplicidad**: La solución más simple que funcione

## Specific Rules

### Naming Conventions

- Variables y funciones: `camelCase` o `snake_case` (según lenguaje)
- Clases: `PascalCase`
- Constantes: `UPPER_SNAKE_CASE`
- Archivos: `kebab-case.extension`

### Code Organization

- Máximo 400 líneas por archivo
- Una clase/módulo por archivo
- Imports al inicio
- Código organizado lógicamente

### Comments

- Código autoexplicativo > comentarios
- Comentar el "por qué", no el "qué"
- Mantener comentarios actualizados
- Eliminar código comentado

### Error Handling

- Siempre manejar errores
- Usar try/catch apropiadamente
- Mensajes de error descriptivos
- Log de errores en producción

## Enforcement

- Linters configurados en el proyecto
- Pre-commit hooks
- Code review checklist
- CI/CD checks

## Examples

### ✅ Good

```javascript
function calculateTotalPrice(items, taxRate) {
  const subtotal = items.reduce((sum, item) => sum + item.price, 0);
  return subtotal * (1 + taxRate);
}
```

### ❌ Bad

```javascript
function calc(i, t) {
  let s = 0;
  for(let x of i) s += x.p;
  return s * (1 + t);
}
```
"""
        
        rule_file = self.output_dir / "rules" / "common" / "coding-style.md"
        with open(rule_file, 'w') as f:
            f.write(content.strip())
        
        print(f"  ✅ {rule_file}")
    
    def _copy_schemas(self):
        """Copia o genera schemas"""
        # En un caso real, copiarías los schemas desde templates
        # Por ahora, solo creamos un placeholder
        schema_file = self.output_dir / "schemas" / "config.schema.json"
        with open(schema_file, 'w') as f:
            f.write('{\n  "$schema": "http://json-schema.org/draft-07/schema#"\n}\n')
        
        print(f"  ✅ {schema_file}")
    
    def _generate_hooks(self):
        """Genera configuración de hooks"""
        hooks = {
            "pre-commit": {
                "enabled": True,
                "script": "scripts/hooks/pre-commit.sh",
                "description": "Run linting and tests before commit"
            },
            "post-merge": {
                "enabled": False,
                "script": "scripts/hooks/post-merge.sh",
                "description": "Update dependencies after merge"
            }
        }
        
        hooks_file = self.output_dir / "hooks" / "hooks.json"
        with open(hooks_file, 'w') as f:
            json.dump(hooks, f, indent=2)
        
        print(f"  ✅ {hooks_file}")
    
    def _get_recommended_skills(self, language: str, framework: str = None):
        """Retorna skills recomendados según lenguaje/framework"""
        skills_map = {
            "java": ["java-coding-standards", "backend-patterns"],
            "python": ["python-patterns", "python-testing", "backend-patterns"],
            "javascript": ["frontend-patterns", "coding-standards"],
            "typescript": ["frontend-patterns", "coding-standards"],
            "go": ["golang-patterns", "golang-testing", "backend-patterns"]
        }
        
        framework_skills = {
            "spring-boot": ["springboot-patterns", "springboot-tdd", "springboot-security"],
            "django": ["django-patterns", "django-tdd", "django-security"],
            "react": ["frontend-patterns"],
            "vue": ["frontend-patterns"],
        }
        
        skills = skills_map.get(language, ["coding-standards"])
        
        if framework:
            skills.extend(framework_skills.get(framework, []))
        
        # Agregar skills comunes
        skills.extend(["tdd-workflow", "verification-loop", "security-review"])
        
        return list(set(skills))  # Eliminar duplicados
    
    def _get_recommended_agents(self, language: str, framework: str = None):
        """Retorna agentes recomendados"""
        base_agents = [
            "planner",
            "code-reviewer",
            "tdd-guide",
            "security-reviewer"
        ]
        
        language_agents = {
            "python": ["python-reviewer"],
            "go": ["go-reviewer", "go-build-resolver"],
            "java": ["database-reviewer"]
        }
        
        agents = base_agents.copy()
        agents.extend(language_agents.get(language, []))
        
        return agents
    
    def _infer_build_tool(self, language: str):
        """Infiere build tool según lenguaje"""
        tools = {
            "javascript": "npm",
            "typescript": "npm",
            "python": "pip",
            "java": "gradle",
            "go": "go",
            "rust": "cargo"
        }
        return tools.get(language, "npm")
    
    def _print_next_steps(self):
        """Imprime pasos siguientes"""
        print("\n" + "="*60)
        print("🎯 PRÓXIMOS PASOS")
        print("="*60)
        print("\n1. Personaliza la configuración:")
        print(f"   vim {self.output_dir}/claude_config.json")
        print(f"   vim {self.output_dir}/CLAUDE.md")
        print("\n2. Revisa y ajusta los skills habilitados")
        print("\n3. Agrega reglas específicas de tu proyecto")
        print("\n4. Valida la configuración:")
        print("   python3 validate_claude_config.py")
        print("\n5. Commitea la configuración:")
        print("   git add .claude/")
        print("   git commit -m 'feat: add Claude Code configuration'")
        print("\n✨ ¡Listo para usar Claude Code!")


def main():
    parser = argparse.ArgumentParser(
        description="Genera configuración de Claude Code"
    )
    parser.add_argument(
        "--project-name",
        required=True,
        help="Nombre del proyecto"
    )
    parser.add_argument(
        "--language",
        required=True,
        choices=["java", "python", "javascript", "typescript", "go", "rust"],
        help="Lenguaje principal del proyecto"
    )
    parser.add_argument(
        "--framework",
        help="Framework usado (spring-boot, django, react, etc.)"
    )
    parser.add_argument(
        "--build-tool",
        choices=["npm", "yarn", "pnpm", "bun", "gradle", "maven", "pip", "go", "cargo"],
        help="Build tool (se infiere del lenguaje si no se especifica)"
    )
    parser.add_argument(
        "--output-dir",
        default=".claude",
        help="Directorio de salida (default: .claude)"
    )
    
    args = parser.parse_args()
    
    generator = ClaudeConfigGenerator(args.output_dir)
    generator.generate(
        args.project_name,
        args.language,
        args.framework,
        args.build_tool
    )


if __name__ == "__main__":
    main()
