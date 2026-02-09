# Guía de Configuración MCP en Kiro

## ¿Qué es MCP?

**Model Context Protocol (MCP)** es un protocolo estándar que permite a los asistentes de IA conectarse con herramientas externas, APIs y servicios. Es **totalmente compatible** entre Claude Code y Kiro.

## Ubicaciones de Configuración

### Claude Code
```
~/.claude.json                    # User-level (global)
.claude/mcp.json                  # Project-level
```

### Kiro
```
~/.kiro/settings/mcp.json         # User-level (global)
.kiro/settings/mcp.json           # Project-level (workspace)
```

## Estructura del Archivo MCP

```json
{
  "mcpServers": {
    "server-name": {
      "command": "comando-a-ejecutar",
      "args": ["argumentos", "del", "comando"],
      "env": {
        "VARIABLE_ENTORNO": "valor"
      },
      "disabled": false,
      "autoApprove": ["tool-name-1", "tool-name-2"]
    }
  }
}
```

### Campos Explicados

- **`command`**: Comando para ejecutar el servidor MCP (ej: `uvx`, `node`, `python`)
- **`args`**: Argumentos del comando (ej: nombre del paquete, ruta al script)
- **`env`**: Variables de entorno (API keys, tokens, configuración)
- **`disabled`**: `true` para deshabilitar sin eliminar la configuración
- **`autoApprove`**: Lista de herramientas que no requieren confirmación manual

## Servidores MCP Populares

### 1. GitHub
```json
{
  "mcpServers": {
    "github": {
      "command": "uvx",
      "args": ["mcp-server-github"],
      "env": {
        "GITHUB_TOKEN": "ghp_your_token_here"
      }
    }
  }
}
```

**Capacidades:**
- Crear/editar issues
- Gestionar PRs
- Buscar código
- Gestionar repositorios

**Obtener token:**
1. Ve a https://github.com/settings/tokens
2. Generate new token (classic)
3. Permisos: `repo`, `read:org`, `read:user`

### 2. Supabase
```json
{
  "mcpServers": {
    "supabase": {
      "command": "uvx",
      "args": ["mcp-server-supabase"],
      "env": {
        "SUPABASE_URL": "https://your-project.supabase.co",
        "SUPABASE_SERVICE_ROLE_KEY": "your_service_role_key"
      }
    }
  }
}
```

**Capacidades:**
- Ejecutar queries SQL
- Gestionar tablas
- Autenticación
- Storage

### 3. PostgreSQL
```json
{
  "mcpServers": {
    "postgres": {
      "command": "uvx",
      "args": ["mcp-server-postgres"],
      "env": {
        "DATABASE_URL": "postgresql://user:password@localhost:5432/dbname"
      }
    }
  }
}
```

**Capacidades:**
- Ejecutar queries
- Gestionar esquemas
- Migraciones
- Análisis de datos

### 4. Filesystem
```json
{
  "mcpServers": {
    "filesystem": {
      "command": "uvx",
      "args": ["mcp-server-filesystem", "/path/to/allowed/directory"],
      "env": {}
    }
  }
}
```

**Capacidades:**
- Leer/escribir archivos
- Listar directorios
- Buscar archivos
- Operaciones de archivos

### 5. Brave Search
```json
{
  "mcpServers": {
    "brave-search": {
      "command": "uvx",
      "args": ["mcp-server-brave-search"],
      "env": {
        "BRAVE_API_KEY": "your_api_key"
      }
    }
  }
}
```

**Capacidades:**
- Búsqueda web
- Búsqueda de noticias
- Información actualizada

### 6. Slack
```json
{
  "mcpServers": {
    "slack": {
      "command": "uvx",
      "args": ["mcp-server-slack"],
      "env": {
        "SLACK_BOT_TOKEN": "xoxb-your-token",
        "SLACK_TEAM_ID": "T1234567890"
      }
    }
  }
}
```

**Capacidades:**
- Enviar mensajes
- Leer canales
- Gestionar usuarios
- Notificaciones

### 7. Google Drive
```json
{
  "mcpServers": {
    "gdrive": {
      "command": "uvx",
      "args": ["mcp-server-gdrive"],
      "env": {
        "GDRIVE_CLIENT_ID": "your_client_id",
        "GDRIVE_CLIENT_SECRET": "your_client_secret",
        "GDRIVE_REFRESH_TOKEN": "your_refresh_token"
      }
    }
  }
}
```

**Capacidades:**
- Leer/escribir documentos
- Gestionar archivos
- Compartir
- Búsqueda

### 8. AWS
```json
{
  "mcpServers": {
    "aws": {
      "command": "uvx",
      "args": ["mcp-server-aws"],
      "env": {
        "AWS_ACCESS_KEY_ID": "your_access_key",
        "AWS_SECRET_ACCESS_KEY": "your_secret_key",
        "AWS_REGION": "us-east-1"
      }
    }
  }
}
```

**Capacidades:**
- S3 operations
- Lambda functions
- EC2 management
- CloudWatch logs

## Instalación de `uvx`

La mayoría de servidores MCP usan `uvx` (parte de `uv`, un gestor de paquetes Python):

### Linux/macOS
```bash
curl -LsSf https://astral.sh/uv/install.sh | sh
```

### Windows
```powershell
powershell -c "irm https://astral.sh/uv/install.ps1 | iex"
```

### Con pip
```bash
pip install uv
```

### Verificar instalación
```bash
uvx --version
```

## Configuración en Kiro

### Opción 1: User-level (Global)

Crea o edita `~/.kiro/settings/mcp.json`:

```bash
mkdir -p ~/.kiro/settings
nano ~/.kiro/settings/mcp.json
```

**Ventajas:**
- Disponible en todos los proyectos
- Configurar una vez, usar siempre
- Ideal para herramientas personales (GitHub, Slack)

### Opción 2: Project-level (Workspace)

Crea o edita `.kiro/settings/mcp.json`:

```bash
mkdir -p .kiro/settings
nano .kiro/settings/mcp.json
```

**Ventajas:**
- Específico del proyecto
- Compartible con el equipo (via git)
- Ideal para bases de datos del proyecto, APIs específicas

### Opción 3: Multi-workspace

En workspaces con múltiples carpetas, cada una puede tener su config:

```
workspace/
├── frontend/.kiro/settings/mcp.json
├── backend/.kiro/settings/mcp.json
└── shared/.kiro/settings/mcp.json
```

**Precedencia:** User < workspace1 < workspace2 < ...

## Ejemplo Completo: Everything Claude Code

Aquí está la configuración completa del repo everything-claude-code adaptada para Kiro:

```json
{
  "mcpServers": {
    "github": {
      "command": "uvx",
      "args": ["mcp-server-github"],
      "env": {
        "GITHUB_TOKEN": "ghp_YOUR_TOKEN_HERE"
      },
      "disabled": false,
      "autoApprove": ["search_repositories", "get_file_contents"]
    },
    "supabase": {
      "command": "uvx",
      "args": ["mcp-server-supabase"],
      "env": {
        "SUPABASE_URL": "https://YOUR_PROJECT.supabase.co",
        "SUPABASE_SERVICE_ROLE_KEY": "YOUR_SERVICE_ROLE_KEY"
      },
      "disabled": false
    },
    "postgres": {
      "command": "uvx",
      "args": ["mcp-server-postgres"],
      "env": {
        "DATABASE_URL": "postgresql://user:password@localhost:5432/dbname"
      },
      "disabled": true
    },
    "filesystem": {
      "command": "uvx",
      "args": ["mcp-server-filesystem", "/home/user/projects"],
      "disabled": false,
      "autoApprove": ["read_file", "list_directory"]
    },
    "brave-search": {
      "command": "uvx",
      "args": ["mcp-server-brave-search"],
      "env": {
        "BRAVE_API_KEY": "YOUR_BRAVE_API_KEY"
      },
      "disabled": false
    },
    "slack": {
      "command": "uvx",
      "args": ["mcp-server-slack"],
      "env": {
        "SLACK_BOT_TOKEN": "xoxb-YOUR_TOKEN",
        "SLACK_TEAM_ID": "T1234567890"
      },
      "disabled": true
    }
  }
}
```

## Gestión de Servidores MCP

### Ver servidores activos

En Kiro, puedes ver los servidores MCP en el panel de MCP Server view.

### Deshabilitar temporalmente

```json
{
  "mcpServers": {
    "github": {
      "command": "uvx",
      "args": ["mcp-server-github"],
      "disabled": true  // ← Deshabilitar sin eliminar
    }
  }
}
```

### Auto-aprobar herramientas

Para herramientas seguras que usas frecuentemente:

```json
{
  "mcpServers": {
    "github": {
      "command": "uvx",
      "args": ["mcp-server-github"],
      "autoApprove": [
        "search_repositories",
        "get_file_contents",
        "list_commits"
      ]
    }
  }
}
```

### Reconectar servidores

Los servidores se reconectan automáticamente cuando:
- Cambias el archivo `mcp.json`
- Reinicias Kiro
- Usas el comando "Reconnect MCP Server" en el panel

## Gestión del Context Window

⚠️ **IMPORTANTE:** No habilites todos los MCPs a la vez.

### Problema
- Context window: 200k tokens
- Con muchos MCPs: puede reducirse a 70k tokens
- Cada MCP agrega herramientas al contexto

### Solución
```json
{
  "mcpServers": {
    // Habilita solo 5-10 servidores a la vez
    "github": { "disabled": false },      // ✅ Habilitado
    "supabase": { "disabled": false },    // ✅ Habilitado
    "postgres": { "disabled": true },     // ❌ Deshabilitado
    "slack": { "disabled": true },        // ❌ Deshabilitado
    "aws": { "disabled": true }           // ❌ Deshabilitado
  }
}
```

### Regla de oro
- **Configurados:** 20-30 MCPs
- **Habilitados por proyecto:** < 10 MCPs
- **Herramientas activas:** < 80 tools

## Debugging MCP

### Ver logs de MCP

Kiro muestra logs de MCP en la consola de desarrollo.

### Probar conexión

```
Test the GitHub MCP connection by searching for repositories
```

### Errores comunes

1. **"Command not found: uvx"**
   - Solución: Instalar `uv` (ver sección de instalación)

2. **"Authentication failed"**
   - Solución: Verificar API keys/tokens en `env`

3. **"Too many tools"**
   - Solución: Deshabilitar MCPs no usados

4. **"Server timeout"**
   - Solución: Verificar conectividad, reiniciar servidor

## Migración desde Claude Code

### Paso 1: Localizar config de Claude
```bash
cat ~/.claude.json
# o
cat .claude/mcp.json
```

### Paso 2: Copiar a Kiro
```bash
# User-level
mkdir -p ~/.kiro/settings
cp ~/.claude.json ~/.kiro/settings/mcp.json

# Project-level
mkdir -p .kiro/settings
cp .claude/mcp.json .kiro/settings/mcp.json
```

### Paso 3: Ajustar formato (si es necesario)

Claude Code a veces usa formato diferente. Asegúrate de que esté bajo la clave `mcpServers`:

```json
{
  "mcpServers": {
    // ... tus servidores aquí
  }
}
```

### Paso 4: Verificar

Reinicia Kiro y verifica que los servidores aparezcan en el panel MCP.

## Recursos

- [MCP Protocol Docs](https://modelcontextprotocol.io)
- [MCP Servers Registry](https://github.com/modelcontextprotocol/servers)
- [Everything Claude Code MCP Configs](https://github.com/affaan-m/everything-claude-code/tree/main/mcp-configs)
- [Kiro MCP Documentation](https://docs.kiro.ai/mcp)

## Servidores MCP Recomendados por Proyecto

### Proyecto Web Full-Stack
```json
{
  "mcpServers": {
    "github": { "disabled": false },
    "supabase": { "disabled": false },
    "filesystem": { "disabled": false },
    "brave-search": { "disabled": false }
  }
}
```

### Proyecto Backend/API
```json
{
  "mcpServers": {
    "github": { "disabled": false },
    "postgres": { "disabled": false },
    "aws": { "disabled": false },
    "slack": { "disabled": false }
  }
}
```

### Proyecto Data Science
```json
{
  "mcpServers": {
    "github": { "disabled": false },
    "postgres": { "disabled": false },
    "filesystem": { "disabled": false },
    "gdrive": { "disabled": false }
  }
}
```

### Proyecto DevOps
```json
{
  "mcpServers": {
    "github": { "disabled": false },
    "aws": { "disabled": false },
    "slack": { "disabled": false },
    "filesystem": { "disabled": false }
  }
}
```

## Crear tu Propio Servidor MCP

Si necesitas integrar una herramienta personalizada:

1. **Usar el SDK de MCP:**
   ```bash
   npm install @modelcontextprotocol/sdk
   ```

2. **Crear servidor:**
   ```typescript
   import { Server } from '@modelcontextprotocol/sdk/server/index.js';
   
   const server = new Server({
     name: 'my-custom-server',
     version: '1.0.0'
   });
   
   server.setRequestHandler('tools/list', async () => ({
     tools: [
       {
         name: 'my_tool',
         description: 'Does something useful',
         inputSchema: {
           type: 'object',
           properties: {
             param: { type: 'string' }
           }
         }
       }
     ]
   }));
   ```

3. **Configurar en Kiro:**
   ```json
   {
     "mcpServers": {
       "my-custom": {
         "command": "node",
         "args": ["/path/to/my-server.js"]
       }
     }
   }
   ```

## Conclusión

Los servidores MCP son **totalmente compatibles** entre Claude Code y Kiro. Solo necesitas:

1. Copiar la configuración a la ubicación correcta
2. Instalar `uvx` si no lo tienes
3. Configurar tus API keys
4. Gestionar qué servidores habilitar por proyecto

¡Los MCPs son una de las características más poderosas para extender las capacidades de Kiro!
