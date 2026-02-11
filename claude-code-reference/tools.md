# Claude Code Tools

## Available Tools

| Tool | Purpose | Requires Permission |
|------|---------|---------------------|
| AskUserQuestion | Multiple-choice questions for requirements | No |
| Bash | Execute shell commands | Yes |
| TaskOutput | Get output from background task | No |
| Edit | Targeted file edits | Yes |
| ExitPlanMode | Prompt user to exit plan mode | Yes |
| Glob | Find files by pattern | No |
| Grep | Search patterns in files | No |
| KillShell | Kill background bash shell | No |
| MCPSearch | Search for MCP tools | No |
| NotebookEdit | Modify Jupyter notebooks | Yes |
| Read | Read file contents | No |
| Skill | Execute a skill | Yes |
| Task | Run sub-agent for complex tasks | No |
| TaskCreate | Create task in task list | No |
| TaskGet | Get task details | No |
| TaskList | List all tasks | No |
| TaskUpdate | Update task status | No |
| WebFetch | Fetch URL content | Yes |
| WebSearch | Web search with domain filtering | Yes |
| Write | Create or overwrite files | Yes |
| LSP | Code intelligence via language servers | No |

## Bash Tool Behavior

### Persistence
- **Working directory persists**: `cd` changes affect subsequent commands
- **Environment variables do NOT persist**: Each command runs in fresh shell

### Making Environment Variables Persist

**Option 1: Activate before starting Claude Code**
```bash
conda activate myenv
claude
```

**Option 2: Set CLAUDE_ENV_FILE**
```bash
export CLAUDE_ENV_FILE=/path/to/env-setup.sh
claude
```

**Option 3: SessionStart hook** (in `.claude/settings.json`)
```json
{
  "hooks": {
    "SessionStart": [{
      "matcher": "startup",
      "hooks": [{
        "type": "command",
        "command": "echo 'conda activate myenv' >> \"$CLAUDE_ENV_FILE\""
      }]
    }]
  }
}
```

## Permission Configuration

### Via settings.json
```json
{
  "permissions": {
    "allow": ["Bash(npm run *)"],
    "deny": ["Read(./.env)", "Bash(curl *)"]
  }
}
```

### Via /allowed-tools command
Interactive configuration during session.

## Tool-Specific Patterns

| Tool | Pattern Examples |
|------|------------------|
| Bash | `Bash(npm run *)`, `Bash(git diff *)` |
| Read | `Read(./.env)`, `Read(./secrets/**)` |
| Edit | `Edit(./config/*)` |
| WebFetch | `WebFetch(domain:example.com)` |
| Task | `Task(agent:security-reviewer)` |

## Extending Tools with Hooks

Run custom commands before/after any tool:
- Auto-format after Python edits
- Block writes to production configs
- Log all bash commands
