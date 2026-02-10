#!/usr/bin/env python3
"""
Generador de configuración de Claude Code
Crea estructura completa de .claude/ con archivos base
"""

import json
import argparse
import shutil
from pathlib import Path
from datetime import datetime

class ClaudeConfigGenerator:
    def __init__(self, output_dir: str = ".claude"):
        self.output_dir = Path(output_dir)
        self.templates_dir = Path(__file__).resolve().parent.parent / "stubs"
        self.repo_root = Path(__file__).resolve().parents[3]
        
    def generate(self, project_name: str, language: str, framework: str = None, 
                 build_tool: str = None):
        """Genera configuración completa"""
        print(f"🚀 Generando configuración de Claude Code para '{project_name}'...\n")
        
        # Crear estructura de directorios
        self._create_directory_structure()
        
        # Generar archivos de configuración
        self._copy_config_json()
        self._copy_claude_md_files()
        self._generate_package_manager_json(build_tool or self._infer_build_tool(language))
        
      
        
        # Generar schemas
        self._copy_schemas()
        
        # Generar hooks
        self._generate_hooks()
        
        print("\n✅ Configuración generada exitosamente!")
        print(f"\n📁 Archivos creados en: {self.output_dir}/")
        self._print_next_steps()
    
    def _create_directory_structure(self):
        """Crea estructura de directorios"""
        self.output_dir.mkdir(parents=True, exist_ok=True)
        stub_destinations = {
            ".claude": self.output_dir,
            ".claude-plugin": self.output_dir.parent / ".claude-plugin"
        }
        for stub_dir, destination_dir in stub_destinations.items():
            source_dir = self.templates_dir / stub_dir
            if source_dir.is_dir():
                destination_dir.mkdir(parents=True, exist_ok=True)
                shutil.copytree(source_dir, destination_dir, dirs_exist_ok=True)
                print(f"  ✅ {destination_dir}")

        dirs = [
            'agents',
            'commands',
            'skills',
            'rules',
            'hooks',
            'schemas',
            'scripts/ci',
            'scripts/hooks'
        ]
        
        for dir_name in dirs:
            dir_path = self.output_dir / dir_name
            source_dir = self.repo_root / dir_name
            if source_dir.is_dir():
                dir_path.parent.mkdir(parents=True, exist_ok=True)
                shutil.copytree(source_dir, dir_path, dirs_exist_ok=True)
                print(f"  ✅ {dir_path}")
            else:
                dir_path.mkdir(parents=True, exist_ok=True)
                print(f"  📁 {dir_path}")
    
    def _copy_config_json(self):
        """Copia claude_config.json desde los templates"""
        source_file = self.templates_dir / "config.json"
        config_file = self.output_dir / "claude_config.json"
        config_file.write_text(source_file.read_text())
        print(f"  ✅ {config_file}")

    def _copy_claude_md_files(self):
        """Copia CLAUDE.MD y user-CLAUDE.MD desde los templates"""
        source_claude_md = self.templates_dir / "CLAUDE.md"
        source_user_claude_md = self.templates_dir / "user-CLAUDE.md"

        claude_md_file = self.output_dir / "CLAUDE.MD"
        user_claude_md_file = self.output_dir / "user-CLAUDE.MD"

        claude_md_file.write_text(source_claude_md.read_text())
        user_claude_md_file.write_text(source_user_claude_md.read_text())

        print(f"  ✅ {claude_md_file}")
        print(f"  ✅ {user_claude_md_file}")
    
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
        default="MyProject",
        help="Nombre del proyecto"
    )
    parser.add_argument(
        "--language",
        default="java",
        choices=["java", "javascript", "typescript"],
        help="Lenguaje principal del proyecto"
    )
    parser.add_argument(
        "--framework",
        default="spring-boot",
        help="Framework usado (spring-boot, django, react, etc.)"
    )
    parser.add_argument(
        "--build-tool",
        choices=["npm", "yarn", "gradle", "maven"],
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
