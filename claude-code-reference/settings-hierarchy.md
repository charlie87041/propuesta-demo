# Claude Code Settings Hierarchy

## Configuration Scopes

| Scope | Location | Shared With | Committed to Git |
|-------|----------|-------------|------------------|
| Managed | System directories | All users on machine | Yes (deployed by IT) |
| User | `~/.claude/` | You, across all projects | No |
| Project | `.claude/` in repository | All collaborators | Yes |
| Local | `.claude/*.local.*` files | You, in this repo only | No (gitignored) |

## Settings Files

| Scope | File |
|-------|------|
| User settings | `~/.claude/settings.json` |
| Project settings | `.claude/settings.json` |
| Local settings | `.claude/settings.local.json` |
| Managed settings | `/etc/claude-code/managed-settings.json` (Linux) |

## Precedence (Highest to Lowest)

1. **Managed** - Cannot be overridden
2. **Command line arguments** - Temporary session overrides
3. **Local** - Overrides project and user
4. **Project** - Overrides user
5. **User** - Applies when nothing else specifies

## What Uses Scopes

| Feature | User | Project | Local |
|---------|------|---------|-------|
| Settings | `~/.claude/settings.json` | `.claude/settings.json` | `.claude/settings.local.json` |
| Subagents | `~/.claude/agents/` | `.claude/agents/` | — |
| MCP servers | `~/.claude.json` | `.mcp.json` | `~/.claude.json` (per-project) |
| CLAUDE.md | `~/.claude/CLAUDE.md` | `CLAUDE.md` or `.claude/CLAUDE.md` | `CLAUDE.local.md` |

## Example settings.json

```json
{
  "$schema": "https://json.schemastore.org/claude-code-settings.json",
  "permissions": {
    "allow": [
      "Bash(npm run lint)",
      "Bash(npm run test *)",
      "Read(~/.zshrc)"
    ],
    "deny": [
      "Bash(curl *)",
      "Read(./.env)",
      "Read(./secrets/**)"
    ]
  },
  "env": {
    "CLAUDE_CODE_ENABLE_TELEMETRY": "1"
  }
}
```

## Permission Rules

| Setting | Purpose |
|---------|---------|
| `allow` | Auto-approve matching tool use |
| `ask` | Ask for confirmation |
| `deny` | Block tool use (evaluated first) |

### Rule Syntax

| Pattern | Matches |
|---------|---------|
| `Bash` | All bash commands |
| `Bash(npm run *)` | Commands starting with `npm run` |
| `Read(./.env)` | Reading the .env file |
| `Read(./secrets/**)` | Reading any file under secrets/ |
| `WebFetch(domain:example.com)` | Fetch requests to example.com |

## Key Settings

| Setting | Purpose | Example |
|---------|---------|---------|
| `model` | Override default model | `"claude-sonnet-4-5-20250929"` |
| `cleanupPeriodDays` | Session retention | `20` |
| `language` | Response language | `"japanese"` |
| `outputStyle` | Adjust system prompt | `"Explanatory"` |
| `additionalDirectories` | Extra working directories | `["../docs/"]` |
| `defaultMode` | Default permission mode | `"acceptEdits"` |
