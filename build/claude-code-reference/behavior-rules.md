# Claude Code Behavior Rules

## Core Principles

1. **Memory files are instructions**: CLAUDE.md contains what you write FOR Claude
2. **Auto memory is Claude's notes**: What Claude writes for itself
3. **Rules auto-load**: No manual loading needed for `.claude/rules/`
4. **Specific overrides general**: Project rules override user rules

## Startup Sequence

1. Load managed policy CLAUDE.md (if exists)
2. Load user memory (`~/.claude/CLAUDE.md`)
3. Load user rules (`~/.claude/rules/*.md`)
4. Load project memory (`./CLAUDE.md` or `./.claude/CLAUDE.md`)
5. Load project rules (`./.claude/rules/**/*.md`)
6. Load local memory (`./CLAUDE.local.md`)
7. Load auto memory (first 200 lines of `MEMORY.md`)

## On-Demand Loading

- Child directory CLAUDE.md files load when reading files there
- Auto memory topic files load when needed
- Skills load on invocation or automatic detection

## Conflict Resolution

When rules conflict:
1. **Managed settings always win** (cannot be overridden)
2. **More specific scope wins**: Local > Project > User
3. **Specialized rules override general**: security.md > CLAUDE.md
4. **Deny rules evaluated first** in permissions

## Permission Behavior

| Rule Type | When Matched |
|-----------|--------------|
| `deny` | Block immediately, no prompt |
| `ask` | Prompt user for confirmation |
| `allow` | Execute without prompting |

Evaluation order: deny first, then ask, then allow.

## Environment Variables That Affect Behavior

| Variable | Effect |
|----------|--------|
| `CLAUDE_CODE_DISABLE_AUTO_MEMORY` | Disable auto memory (1=off, 0=force on) |
| `CLAUDE_BASH_MAINTAIN_PROJECT_WORKING_DIR` | Reset to project dir after each bash |
| `ANTHROPIC_MODEL` | Override default model |
| `MAX_THINKING_TOKENS` | Control extended thinking budget |
| `CLAUDE_CODE_EFFORT_LEVEL` | low/medium/high (Opus 4.6 only) |

## Key Behavioral Notes

### File Reading
- CLAUDE.md imports use `@path/to/file` syntax
- Imports don't evaluate inside code blocks
- Max import depth: 5 hops

### Tool Usage
- Bash working directory persists across commands
- Environment variables do NOT persist across bash commands
- Each bash command runs in fresh shell

### Context Management
- Auto-compaction triggers at ~95% context capacity
- Override with `CLAUDE_AUTOCOMPACT_PCT_OVERRIDE`
- Auto memory keeps `MEMORY.md` under 200 lines

### Security
- Permission deny rules cannot be overridden by lower scopes
- Managed settings have absolute precedence
- Sensitive files should use `permissions.deny` in settings

## Command Prefixes

| Prefix | Behavior |
|--------|----------|
| `DEBUG:` | No execution. Output a Reasoning Trace instead. |

### DEBUG: Reasoning Trace Format

When a request starts with `DEBUG:`, do NOT execute any file edits or commands. Instead, output:

1. **Intention Analysis**: What the user wants to achieve
2. **Context Audit**:
   - **Active**: Files currently in context
   - **Discovered**: Files found via search but not read
   - **Missing**: Expected files that are missing
3. **Rule Application**: Which rules are influencing decisions
4. **Planned Chain of Thought**: Step-by-step logic of what would be done
5. **Agent Delegation**: Which specialized agents would be spawned
6. **Skills**: Which skills would be applied

## Common Mistakes to Avoid

1. **Manually loading rules**: Rules in `.claude/rules/` auto-load
2. **Expecting env vars to persist**: Use CLAUDE_ENV_FILE instead
3. **Putting secrets in CLAUDE.md**: Use permissions.deny for sensitive files
4. **Giant CLAUDE.md files**: Use modular rules in `.claude/rules/`
5. **Ignoring precedence**: Know that project overrides user settings
6. **Ignoring command prefixes**: Check for `DEBUG:` before executing
