# Claude Code Memory System

## Memory Types and Locations

| Type | Location | Auto-loaded | Scope |
|------|----------|-------------|-------|
| Managed policy | `/etc/claude-code/CLAUDE.md` (Linux) | Yes | Organization-wide |
| Project memory | `./CLAUDE.md` or `./.claude/CLAUDE.md` | Yes | Team (via git) |
| Project rules | `./.claude/rules/*.md` | Yes (recursive) | Team (via git) |
| User memory | `~/.claude/CLAUDE.md` | Yes | Personal (all projects) |
| User rules | `~/.claude/rules/*.md` | Yes | Personal (all projects) |
| Local project | `./CLAUDE.local.md` | Yes | Personal (current project) |
| Auto memory | `~/.claude/projects/<project>/memory/MEMORY.md` | Yes (first 200 lines) | Personal (per project) |

## Loading Behavior

### Startup Loading
- CLAUDE.md files in directory hierarchy (cwd upward) load in full
- All `.md` files in `.claude/rules/` load recursively
- User-level rules load before project rules (project takes precedence)
- Auto memory loads only first 200 lines of `MEMORY.md`

### On-Demand Loading
- CLAUDE.md files in child directories load when Claude reads files in those directories
- Auto memory topic files (e.g., `debugging.md`) load when Claude needs them

## Path-Specific Rules

Rules can target specific files using YAML frontmatter:

```yaml
---
paths:
  - "src/api/**/*.ts"
  - "src/**/*.{ts,tsx}"
---

# API Rules
- All endpoints must include input validation
```

### Glob Patterns Supported

| Pattern | Matches |
|---------|---------|
| `**/*.ts` | All TypeScript files in any directory |
| `src/**/*` | All files under src/ |
| `*.md` | Markdown files in project root |
| `src/**/*.{ts,tsx}` | TypeScript and TSX files under src/ |
| `{src,lib}/**/*.ts` | TypeScript files in src/ or lib/ |

## Imports

CLAUDE.md files can import other files:

```markdown
See @README for project overview.
Additional instructions: @docs/git-instructions.md
Personal preferences: @~/.claude/my-project-instructions.md
```

- Relative paths resolve from the importing file
- Absolute paths and `~` supported
- Max import depth: 5 hops
- Imports inside code blocks are ignored

## Auto Memory

Claude automatically saves learnings to `~/.claude/projects/<project>/memory/`:

```
memory/
├── MEMORY.md          # Index, loaded at startup (200 lines max)
├── debugging.md       # Topic files, loaded on demand
├── api-conventions.md
└── ...
```

### What Claude Saves
- Project patterns (build commands, test conventions)
- Debugging insights (solutions to problems)
- Architecture notes (key files, module relationships)
- User preferences (workflow habits)

### Control Auto Memory
```bash
export CLAUDE_CODE_DISABLE_AUTO_MEMORY=1  # Force off
export CLAUDE_CODE_DISABLE_AUTO_MEMORY=0  # Force on
```
