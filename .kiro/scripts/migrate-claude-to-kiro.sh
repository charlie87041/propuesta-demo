#!/bin/bash

# Script de migración de Claude Code a Kiro
# Uso: ./migrate-claude-to-kiro.sh [ruta-a-everything-claude-code]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KIRO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
ECC_REPO="${1:-./everything-claude-code}"

echo "🚀 Migración de Claude Code a Kiro"
echo "=================================="
echo ""

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para imprimir con color
print_status() {
    echo -e "${GREEN}✓${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

# Verificar si el repo existe
if [ ! -d "$ECC_REPO" ]; then
    print_error "No se encontró el repositorio everything-claude-code en: $ECC_REPO"
    echo ""
    echo "Opciones:"
    echo "  1. Clonar el repo:"
    echo "     git clone https://github.com/affaan-m/everything-claude-code.git"
    echo ""
    echo "  2. Especificar ruta:"
    echo "     $0 /ruta/a/everything-claude-code"
    exit 1
fi

print_status "Repositorio encontrado: $ECC_REPO"
echo ""

# Crear directorios necesarios
print_info "Creando estructura de directorios..."
mkdir -p "$KIRO_ROOT/skills/commands"
mkdir -p "$KIRO_ROOT/steering"
mkdir -p "$KIRO_ROOT/settings"
mkdir -p "$KIRO_ROOT/hooks"
print_status "Directorios creados"
echo ""

# Migrar Skills
print_info "Migrando skills..."
SKILLS_COPIED=0
if [ -d "$ECC_REPO/skills" ]; then
    for skill_dir in "$ECC_REPO/skills"/*; do
        if [ -d "$skill_dir" ]; then
            skill_name=$(basename "$skill_dir")
            
            # Preguntar al usuario
            read -p "¿Copiar skill '$skill_name'? (s/n/t=todos): " -n 1 -r
            echo
            
            if [[ $REPLY =~ ^[SsYy]$ ]] || [[ $REPLY =~ ^[Tt]$ ]]; then
                cp -r "$skill_dir" "$KIRO_ROOT/skills/"
                print_status "Copiado: $skill_name"
                ((SKILLS_COPIED++))
                
                if [[ $REPLY =~ ^[Tt]$ ]]; then
                    # Copiar todos sin preguntar más
                    for remaining_skill in "$ECC_REPO/skills"/*; do
                        if [ -d "$remaining_skill" ]; then
                            remaining_name=$(basename "$remaining_skill")
                            if [ "$remaining_name" != "$skill_name" ]; then
                                cp -r "$remaining_skill" "$KIRO_ROOT/skills/"
                                print_status "Copiado: $remaining_name"
                                ((SKILLS_COPIED++))
                            fi
                        fi
                    done
                    break
                fi
            fi
        fi
    done
fi
print_status "Skills copiados: $SKILLS_COPIED"
echo ""

# Migrar Rules a Steering
print_info "Migrando rules a steering..."
RULES_COPIED=0

migrate_rule_to_steering() {
    local rule_file="$1"
    local output_file="$2"
    local inclusion="${3:-auto}"
    local file_pattern="${4:-}"
    
    # Crear frontmatter
    echo "---" > "$output_file"
    echo "inclusion: $inclusion" >> "$output_file"
    if [ -n "$file_pattern" ]; then
        echo "fileMatchPattern: '$file_pattern'" >> "$output_file"
    fi
    echo "---" >> "$output_file"
    echo "" >> "$output_file"
    
    # Copiar contenido
    cat "$rule_file" >> "$output_file"
}

# Migrar rules comunes
if [ -d "$ECC_REPO/rules/common" ]; then
    print_info "Migrando rules comunes..."
    for rule_file in "$ECC_REPO/rules/common"/*.md; do
        if [ -f "$rule_file" ]; then
            rule_name=$(basename "$rule_file")
            output_file="$KIRO_ROOT/steering/$rule_name"
            
            migrate_rule_to_steering "$rule_file" "$output_file" "auto"
            print_status "Migrado: $rule_name (siempre incluido)"
            ((RULES_COPIED++))
        fi
    done
fi

# Migrar rules de TypeScript
if [ -d "$ECC_REPO/rules/typescript" ]; then
    print_info "Migrando rules de TypeScript..."
    for rule_file in "$ECC_REPO/rules/typescript"/*.md; do
        if [ -f "$rule_file" ]; then
            rule_name=$(basename "$rule_file")
            output_file="$KIRO_ROOT/steering/ts-$rule_name"
            
            migrate_rule_to_steering "$rule_file" "$output_file" "fileMatch" "**/*.ts"
            print_status "Migrado: ts-$rule_name (solo archivos .ts)"
            ((RULES_COPIED++))
        fi
    done
fi

# Migrar rules de Python
if [ -d "$ECC_REPO/rules/python" ]; then
    print_info "Migrando rules de Python..."
    for rule_file in "$ECC_REPO/rules/python"/*.md; do
        if [ -f "$rule_file" ]; then
            rule_name=$(basename "$rule_file")
            output_file="$KIRO_ROOT/steering/py-$rule_name"
            
            migrate_rule_to_steering "$rule_file" "$output_file" "fileMatch" "**/*.py"
            print_status "Migrado: py-$rule_name (solo archivos .py)"
            ((RULES_COPIED++))
        fi
    done
fi

# Migrar rules de Go
if [ -d "$ECC_REPO/rules/golang" ]; then
    print_info "Migrando rules de Go..."
    for rule_file in "$ECC_REPO/rules/golang"/*.md; do
        if [ -f "$rule_file" ]; then
            rule_name=$(basename "$rule_file")
            output_file="$KIRO_ROOT/steering/go-$rule_name"
            
            migrate_rule_to_steering "$rule_file" "$output_file" "fileMatch" "**/*.go"
            print_status "Migrado: go-$rule_name (solo archivos .go)"
            ((RULES_COPIED++))
        fi
    done
fi

print_status "Rules migrados a steering: $RULES_COPIED"
echo ""

# Migrar MCP config
print_info "Migrando configuración MCP..."
if [ -f "$ECC_REPO/mcp-configs/mcp-servers.json" ]; then
    # Verificar si ya existe
    if [ -f "$KIRO_ROOT/settings/mcp.json" ]; then
        print_warning "Ya existe $KIRO_ROOT/settings/mcp.json"
        read -p "¿Sobrescribir? (s/n): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[SsYy]$ ]]; then
            cp "$ECC_REPO/mcp-configs/mcp-servers.json" "$KIRO_ROOT/settings/mcp.json"
            print_status "MCP config sobrescrito"
        else
            print_info "MCP config no modificado"
        fi
    else
        cp "$ECC_REPO/mcp-configs/mcp-servers.json" "$KIRO_ROOT/settings/mcp.json"
        print_status "MCP config copiado"
    fi
    
    print_warning "IMPORTANTE: Edita $KIRO_ROOT/settings/mcp.json y reemplaza los placeholders:"
    print_warning "  - YOUR_GITHUB_TOKEN"
    print_warning "  - YOUR_SUPABASE_URL"
    print_warning "  - etc."
else
    print_info "No se encontró configuración MCP en el repo"
fi
echo ""

# Información sobre hooks
print_info "Información sobre Hooks..."
print_warning "Los hooks de Claude Code no se pueden migrar automáticamente"
print_info "Kiro usa una sintaxis diferente. Opciones:"
echo "  1. Usar la UI de hooks en Kiro"
echo "  2. Pedirle a Kiro que cree hooks con createHook"
echo "  3. Ver ejemplos en: $KIRO_ROOT/CLAUDE-TO-KIRO-BRIDGE.md"
echo ""

# Resumen
echo ""
echo "=================================="
echo "📊 Resumen de Migración"
echo "=================================="
print_status "Skills copiados: $SKILLS_COPIED"
print_status "Rules migrados: $RULES_COPIED"
print_status "MCP config: $([ -f "$KIRO_ROOT/settings/mcp.json" ] && echo "✓" || echo "✗")"
echo ""

# Próximos pasos
echo "🎯 Próximos Pasos:"
echo ""
echo "1. Revisar skills copiados:"
echo "   ls -la $KIRO_ROOT/skills/"
echo ""
echo "2. Revisar steering files:"
echo "   ls -la $KIRO_ROOT/steering/"
echo ""
echo "3. Configurar MCP (si aplica):"
echo "   nano $KIRO_ROOT/settings/mcp.json"
echo "   # Reemplazar placeholders con tus API keys"
echo ""
echo "4. Instalar uvx para MCP (si no lo tienes):"
echo "   curl -LsSf https://astral.sh/uv/install.sh | sh"
echo ""
echo "5. Crear hooks personalizados:"
echo "   # Usar la UI de hooks en Kiro"
echo "   # O pedirle a Kiro: 'Create a hook that...'"
echo ""
echo "6. Leer la guía completa:"
echo "   cat $KIRO_ROOT/CLAUDE-TO-KIRO-BRIDGE.md"
echo ""

print_status "¡Migración completada!"
