# Skills and Agents

## Skills

### What Are Skills?
Custom prompts that extend Claude's capabilities. Can be:
- Invoked explicitly with `/skill-name`
- Loaded automatically by Claude when relevant

### Skill Locations

| Scope | Location |
|-------|----------|
| User | `~/.claude/skills/` |
| Project | `.claude/skills/` |

### How Skills Load
1. **Explicit invocation**: User types `/skill-name`
2. **Automatic loading**: Claude detects relevant context and loads skill
3. **Via Skill tool**: Claude uses Skill tool to execute within conversation

### Skill Metadata Budget
- Scales dynamically at 2% of context window
- Fallback: 16,000 characters
- Control via `SLASH_COMMAND_TOOL_CHAR_BUDGET` env var

## Subagents

### What Are Subagents?
Specialized AI assistants with custom prompts and tool permissions.

### Subagent Locations

| Scope | Location | Availability |
|-------|----------|--------------|
| User | `~/.claude/agents/` | All your projects |
| Project | `.claude/agents/` | This project only |

### Subagent File Format
Markdown files with YAML frontmatter:

```markdown
---
name: security-reviewer
description: Reviews code for security vulnerabilities
tools:
  - Read
  - Grep
  - Glob
---

# Security Reviewer

You are a security expert. Review code for:
- SQL injection
- XSS vulnerabilities
- Authentication issues
...
```

### Invoking Subagents

1. **Direct invocation**: `/agent-name` or `@agent-name`
2. **Via Task tool**: Claude spawns subagent for complex work
3. **Agent teams**: Multiple agents collaborating (experimental)

## Agent Teams (Experimental)

Enable with:
```bash
export CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS=1
```

Configure display mode via `teammateMode` setting:
- `auto`: Picks split panes in tmux/iTerm2, in-process otherwise
- `in-process`: Run in same process
- `tmux`: Use tmux panes

## Best Practices

### Skills
- Keep focused on one task
- Include clear usage examples
- Document when to use vs not use

### Agents
- Define clear tool permissions
- Scope to specific domains (security, testing, etc.)
- Include context about project conventions
