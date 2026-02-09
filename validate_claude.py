#!/usr/bin/env python3
"""
Script de validación completo para configuración de Claude Code (.claude)
Verifica estructura, consistencia, y mejores prácticas
"""

import os
import json
import re
from pathlib import Path
from typing import Dict, List, Tuple, Set
from dataclasses import dataclass
from collections import defaultdict

@dataclass
class ValidationIssue:
    severity: str  # 'error', 'warning', 'info'
    category: str
    message: str
    file_path: str = ""

class ClaudeConfigValidator:
    def __init__(self, claude_dir: str):
        self.claude_dir = Path(claude_dir)
        self.issues: List[ValidationIssue] = []
        
    def validate_all(self) -> Tuple[bool, List[ValidationIssue]]:
        """Ejecuta todas las validaciones"""
        print("🔍 Iniciando validación de configuración de Claude Code...\n")
        
        # Validaciones estructurales
        self.validate_directory_structure()
        self.validate_required_files()
        
        # Validaciones de contenido
        self.validate_agents()
        self.validate_commands()
        self.validate_skills()
        self.validate_contexts()
        self.validate_rules()
        self.validate_hooks()
        self.validate_schemas()
        
        # Validaciones de consistencia
        self.check_cross_references()
        self.check_naming_conventions()
        self.check_duplicates()
        
        # Reporte
        return self.generate_report()
    
    def validate_directory_structure(self):
        """Valida que existan los directorios esperados"""
        expected_dirs = [
            'agents', 'commands', 'contexts', 'rules', 
            'skills', 'hooks', 'schemas', 'scripts'
        ]
        
        for dir_name in expected_dirs:
            dir_path = self.claude_dir / dir_name
            if not dir_path.exists():
                self.issues.append(ValidationIssue(
                    severity='warning',
                    category='structure',
                    message=f"Directorio recomendado no encontrado: {dir_name}",
                    file_path=str(dir_path)
                ))
    
    def validate_required_files(self):
        """Valida archivos requeridos o recomendados"""
        # CLAUDE.md es el archivo principal
        claude_md = self.claude_dir / 'CLAUDE.md'
        if claude_md.exists():
            self.issues.append(ValidationIssue(
                severity='info',
                category='structure',
                message="✓ Archivo CLAUDE.md encontrado (archivo principal de configuración)"
            ))
        else:
            self.issues.append(ValidationIssue(
                severity='warning',
                category='structure',
                message="CLAUDE.md no encontrado (recomendado como punto de entrada)"
            ))
    
    def validate_agents(self):
        """Valida archivos de agentes"""
        agents_dir = self.claude_dir / 'agents'
        if not agents_dir.exists():
            return
        
        agent_files = list(agents_dir.glob('*.md'))
        print(f"📋 Validando {len(agent_files)} agentes...")
        
        required_sections = ['# Agent:', 'Purpose:', 'Approach:']
        
        for agent_file in agent_files:
            content = agent_file.read_text(encoding='utf-8', errors='ignore')
            
            # Verificar secciones requeridas
            missing_sections = []
            for section in required_sections:
                if section not in content:
                    missing_sections.append(section)
            
            if missing_sections:
                self.issues.append(ValidationIssue(
                    severity='warning',
                    category='agents',
                    message=f"Agente '{agent_file.name}' no tiene secciones: {', '.join(missing_sections)}",
                    file_path=str(agent_file)
                ))
            
            # Verificar que no esté vacío
            if len(content.strip()) < 100:
                self.issues.append(ValidationIssue(
                    severity='warning',
                    category='agents',
                    message=f"Agente '{agent_file.name}' parece estar vacío o incompleto",
                    file_path=str(agent_file)
                ))
    
    def validate_commands(self):
        """Valida archivos de comandos"""
        commands_dir = self.claude_dir / 'commands'
        if not commands_dir.exists():
            return
        
        command_files = list(commands_dir.glob('*.md'))
        print(f"⚡ Validando {len(command_files)} comandos...")
        
        for cmd_file in command_files:
            content = cmd_file.read_text(encoding='utf-8', errors='ignore')
            
            # Los comandos deberían tener una descripción clara
            if '# Command:' not in content and '## Command' not in content:
                self.issues.append(ValidationIssue(
                    severity='info',
                    category='commands',
                    message=f"Comando '{cmd_file.name}' no tiene header estándar",
                    file_path=str(cmd_file)
                ))
            
            # Verificar que tenga contenido útil
            if len(content.strip()) < 50:
                self.issues.append(ValidationIssue(
                    severity='warning',
                    category='commands',
                    message=f"Comando '{cmd_file.name}' parece estar vacío",
                    file_path=str(cmd_file)
                ))
    
    def validate_skills(self):
        """Valida archivos de skills"""
        skills_dir = self.claude_dir / 'skills'
        if not skills_dir.exists():
            return
        
        # Skills pueden estar en subdirectorios
        skill_files = list(skills_dir.rglob('SKILL.md'))
        print(f"🎯 Validando {len(skill_files)} skills...")
        
        for skill_file in skill_files:
            content = skill_file.read_text(encoding='utf-8', errors='ignore')
            
            # Verificar secciones importantes
            important_sections = ['Purpose', 'Usage', 'Example', 'When to Use']
            found_sections = sum(1 for section in important_sections if section.lower() in content.lower())
            
            if found_sections == 0:
                self.issues.append(ValidationIssue(
                    severity='warning',
                    category='skills',
                    message=f"Skill '{skill_file.parent.name}' no tiene secciones descriptivas claras",
                    file_path=str(skill_file)
                ))
            
            # Verificar longitud mínima
            if len(content.strip()) < 200:
                self.issues.append(ValidationIssue(
                    severity='warning',
                    category='skills',
                    message=f"Skill '{skill_file.parent.name}' parece demasiado corto",
                    file_path=str(skill_file)
                ))
    
    def validate_contexts(self):
        """Valida archivos de contexto"""
        contexts_dir = self.claude_dir / 'contexts'
        if not contexts_dir.exists():
            return
        
        context_files = list(contexts_dir.glob('*.md'))
        print(f"📝 Validando {len(context_files)} contextos...")
        
        for ctx_file in context_files:
            content = ctx_file.read_text(encoding='utf-8', errors='ignore')
            
            if len(content.strip()) < 100:
                self.issues.append(ValidationIssue(
                    severity='warning',
                    category='contexts',
                    message=f"Contexto '{ctx_file.name}' parece estar vacío o muy corto",
                    file_path=str(ctx_file)
                ))
    
    def validate_rules(self):
        """Valida archivos de reglas"""
        rules_dir = self.claude_dir / 'rules'
        if not rules_dir.exists():
            return
        
        rule_files = list(rules_dir.rglob('*.md'))
        print(f"📏 Validando {len(rule_files)} reglas...")
        
        for rule_file in rule_files:
            content = rule_file.read_text(encoding='utf-8', errors='ignore')
            
            # Las reglas deberían ser claras y específicas
            if len(content.strip()) < 100:
                self.issues.append(ValidationIssue(
                    severity='warning',
                    category='rules',
                    message=f"Regla '{rule_file.name}' parece estar vacía o muy corta",
                    file_path=str(rule_file)
                ))
    
    def validate_hooks(self):
        """Valida configuración de hooks"""
        hooks_file = self.claude_dir / 'hooks' / 'hooks.json'
        
        if not hooks_file.exists():
            self.issues.append(ValidationIssue(
                severity='info',
                category='hooks',
                message="No se encontró hooks.json (opcional)"
            ))
            return
        
        try:
            with open(hooks_file, 'r') as f:
                hooks_config = json.load(f)
            
            # Validar estructura básica
            if not isinstance(hooks_config, dict):
                self.issues.append(ValidationIssue(
                    severity='error',
                    category='hooks',
                    message="hooks.json debe ser un objeto JSON",
                    file_path=str(hooks_file)
                ))
            else:
                self.issues.append(ValidationIssue(
                    severity='info',
                    category='hooks',
                    message=f"✓ hooks.json válido con {len(hooks_config)} hooks configurados"
                ))
        
        except json.JSONDecodeError as e:
            self.issues.append(ValidationIssue(
                severity='error',
                category='hooks',
                message=f"Error de sintaxis JSON en hooks.json: {str(e)}",
                file_path=str(hooks_file)
            ))
    
    def validate_schemas(self):
        """Valida archivos de esquema JSON"""
        schemas_dir = self.claude_dir / 'schemas'
        if not schemas_dir.exists():
            return
        
        schema_files = list(schemas_dir.glob('*.json'))
        print(f"🔧 Validando {len(schema_files)} esquemas...")
        
        for schema_file in schema_files:
            try:
                with open(schema_file, 'r') as f:
                    json.load(f)
                self.issues.append(ValidationIssue(
                    severity='info',
                    category='schemas',
                    message=f"✓ Schema '{schema_file.name}' válido"
                ))
            except json.JSONDecodeError as e:
                self.issues.append(ValidationIssue(
                    severity='error',
                    category='schemas',
                    message=f"Error en '{schema_file.name}': {str(e)}",
                    file_path=str(schema_file)
                ))
    
    def check_cross_references(self):
        """Verifica referencias cruzadas entre archivos"""
        print("\n🔗 Verificando referencias cruzadas...")
        
        # Recopilar nombres de skills, agents, commands
        skills = set()
        skills_dir = self.claude_dir / 'skills'
        if skills_dir.exists():
            for skill_dir in skills_dir.iterdir():
                if skill_dir.is_dir():
                    skills.add(skill_dir.name)
        
        agents = set()
        agents_dir = self.claude_dir / 'agents'
        if agents_dir.exists():
            agents = {f.stem for f in agents_dir.glob('*.md')}
        
        commands = set()
        commands_dir = self.claude_dir / 'commands'
        if commands_dir.exists():
            commands = {f.stem for f in commands_dir.glob('*.md')}
        
        self.issues.append(ValidationIssue(
            severity='info',
            category='cross-reference',
            message=f"Inventario: {len(skills)} skills, {len(agents)} agents, {len(commands)} commands"
        ))
    
    def check_naming_conventions(self):
        """Verifica convenciones de nombres"""
        print("📛 Verificando convenciones de nombres...")
        
        # Verificar que los nombres sean kebab-case
        for directory in ['agents', 'commands', 'skills', 'contexts', 'rules']:
            dir_path = self.claude_dir / directory
            if not dir_path.exists():
                continue
            
            for item in dir_path.iterdir():
                name = item.stem if item.is_file() else item.name
                
                # Verificar kebab-case (palabras separadas por guiones, minúsculas)
                if not re.match(r'^[a-z0-9]+(-[a-z0-9]+)*$', name):
                    self.issues.append(ValidationIssue(
                        severity='info',
                        category='naming',
                        message=f"'{name}' no sigue kebab-case (recomendado: minúsculas-con-guiones)",
                        file_path=str(item)
                    ))
    
    def check_duplicates(self):
        """Busca posibles duplicados o conflictos"""
        print("🔍 Buscando duplicados...")
        
        # Verificar nombres duplicados en skills
        skills_names = defaultdict(list)
        skills_dir = self.claude_dir / 'skills'
        if skills_dir.exists():
            for skill_dir in skills_dir.iterdir():
                if skill_dir.is_dir():
                    skills_names[skill_dir.name.lower()].append(str(skill_dir))
        
        for name, paths in skills_names.items():
            if len(paths) > 1:
                self.issues.append(ValidationIssue(
                    severity='warning',
                    category='duplicates',
                    message=f"Posible duplicado de skill: {name} encontrado en {len(paths)} ubicaciones"
                ))
    
    def generate_report(self) -> Tuple[bool, List[ValidationIssue]]:
        """Genera reporte final"""
        print("\n" + "="*80)
        print("📊 REPORTE DE VALIDACIÓN")
        print("="*80 + "\n")
        
        # Contar por severidad
        errors = [i for i in self.issues if i.severity == 'error']
        warnings = [i for i in self.issues if i.severity == 'warning']
        infos = [i for i in self.issues if i.severity == 'info']
        
        print(f"❌ Errores: {len(errors)}")
        print(f"⚠️  Advertencias: {len(warnings)}")
        print(f"ℹ️  Información: {len(infos)}")
        print(f"\nTotal de issues: {len(self.issues)}\n")
        
        # Agrupar por categoría
        by_category = defaultdict(list)
        for issue in self.issues:
            by_category[issue.category].append(issue)
        
        # Mostrar errores primero
        if errors:
            print("\n🚨 ERRORES CRÍTICOS:")
            print("-" * 80)
            for issue in errors:
                print(f"  • {issue.message}")
                if issue.file_path:
                    print(f"    📁 {issue.file_path}")
        
        # Luego advertencias
        if warnings:
            print("\n⚠️  ADVERTENCIAS:")
            print("-" * 80)
            for issue in warnings:
                print(f"  • {issue.message}")
                if issue.file_path:
                    print(f"    📁 {issue.file_path}")
        
        # Información al final
        if infos:
            print("\nℹ️  INFORMACIÓN:")
            print("-" * 80)
            for issue in infos:
                print(f"  • {issue.message}")
        
        # Resumen por categoría
        print("\n📋 RESUMEN POR CATEGORÍA:")
        print("-" * 80)
        for category, issues in sorted(by_category.items()):
            print(f"  {category}: {len(issues)} issues")
        
        # Conclusión
        print("\n" + "="*80)
        if len(errors) == 0 and len(warnings) == 0:
            print("✅ VALIDACIÓN EXITOSA - No se encontraron problemas críticos")
            is_valid = True
        elif len(errors) == 0:
            print("⚠️  VALIDACIÓN CON ADVERTENCIAS - Revisa las advertencias arriba")
            is_valid = True
        else:
            print("❌ VALIDACIÓN FALLIDA - Se encontraron errores críticos")
            is_valid = False
        print("="*80 + "\n")
        
        return is_valid, self.issues


def main():
    claude_dir = '/home/claude/.claude'
    
    validator = ClaudeConfigValidator(claude_dir)
    is_valid, issues = validator.validate_all()
    
    # Guardar reporte
    report_file = Path('/home/claude/validation_report.txt')
    with open(report_file, 'w') as f:
        f.write("REPORTE DE VALIDACIÓN - Claude Code Configuration\n")
        f.write("=" * 80 + "\n\n")
        
        for issue in issues:
            f.write(f"[{issue.severity.upper()}] {issue.category}: {issue.message}\n")
            if issue.file_path:
                f.write(f"  File: {issue.file_path}\n")
            f.write("\n")
    
    print(f"📄 Reporte guardado en: {report_file}")
    
    return 0 if is_valid else 1


if __name__ == '__main__':
    exit(main())
