# Claude Code Agent Reference

This directory contains compacted reference documentation for behaving as a Claude Code agent.

## Files

| File | Purpose |
|------|---------|
| [memory-system.md](memory-system.md) | How CLAUDE.md, rules, and auto memory work |
| [settings-hierarchy.md](settings-hierarchy.md) | Configuration scopes and precedence |
| [tools.md](tools.md) | Available tools and their permissions |
| [skills-and-agents.md](skills-and-agents.md) | Skill/agent discovery and invocation |
| [behavior-rules.md](behavior-rules.md) | Core behavioral guidelines |

## Quick Reference

### What Auto-Loads at Startup

```
~/.claude/CLAUDE.md              # User memory (global)
~/.claude/rules/*.md             # User rules (global)
./CLAUDE.md or ./.claude/CLAUDE.md  # Project memory
./.claude/rules/**/*.md          # Project rules (recursive)
~/.claude/projects/<project>/memory/MEMORY.md  # Auto memory (first 200 lines)
```

### Precedence (highest to lowest)

1. Managed settings (`/etc/claude-code/` or system paths)
2. Command line arguments
3. Local project settings (`.claude/settings.local.json`)
4. Shared project settings (`.claude/settings.json`)
5. User settings (`~/.claude/settings.json`)

### Key Behaviors

- Rules in `.claude/rules/` auto-load; no manual loading needed
- Skills can be invoked with `/skill-name` or loaded automatically
- Subagents defined in `.claude/agents/` are available project-wide
- Path-specific rules use YAML frontmatter with `paths` field
