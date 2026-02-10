

## Core Philosophy

You are Claude Code. I use specialized agents and skills for complex tasks.

**Key Principles:**
1. **Agent-First**: Delegate to specialized agents for complex work
2. **Parallel Execution**: Use Task tool with multiple agents when possible
3. **Plan Before Execute**: Use Plan Mode for complex operations
4. **Test-Driven**: Write tests before implementation
5. **Security-First**: Never compromise on security

---

## Modular Rules

Detailed guidelines are in `.claude/rules/`:

| Rule File | Contents |
|-----------|----------|
| security.md | Security checks, secret management |
| coding-style.md | Immutability, file organization, error handling |
| testing.md | TDD workflow, 80% coverage requirement |
| git-workflow.md | Commit format, PR workflow |
| agents.md | Agent orchestration, when to use which agent |
| patterns.md | API response, repository patterns |
| performance.md | Model selection, context management |
| hooks.md | Hooks System |

---

## Available Agents

Located in `.claude/agents/`:

| Agent | Purpose |
|-------|---------|
| planner-tdd | Feature implementation planning |
| architect | System design and architecture |
| tdd-guide | Test-driven development |
| code-reviewer | Code review for quality/security |
| security-reviewer | Security vulnerability analysis |
| build-error-resolver | Build error resolution |
| e2e-runner | Playwright E2E testing |
| refactor-cleaner | Dead code cleanup |
| doc-updater | Documentation updates |

---

## Personal Preferences

### Privacy
- Always redact logs; never paste secrets (API keys/tokens/passwords/JWTs)
- Review output before sharing - remove any sensitive data

### Code Style
- No emojis in code, comments, or documentation
- Prefer immutability - never mutate objects or arrays
- Many small files over few large files
- 200-400 lines typical, 800 max per file

### Git
- Conventional commits: `feat:`, `fix:`, `refactor:`, `docs:`, `test:`
- Always test locally before committing
- Small, focused commits

### Testing
- TDD: Write tests first
- 80% minimum coverage
- Unit + integration + E2E for critical flows

---

### Context Loading Protocol
* **Pre-flight**: Before the first edit of any session, run `cat` on `.claude/rules/coding-style.md` to refresh active memory.
* **Conflict Resolution**: If a requirement in a specialized rule (e.g., `security.md`) conflicts with a general preference, the **specialized rule** always takes precedence.

---

### Skill Discovery Protocol

Before executing any agent command, automatically load relevant skills using these rules:

#### Agent-to-Skill Mapping

| Agent | Auto-load Skills |
|-------|------------------|
| architect | springboot-patterns, backend-patterns, jpa-patterns, postgres-patterns |
| planner-tdd | tdd-workflow, tdd-milestone-planning, springboot-tdd |
| tdd-guide | tdd-workflow, springboot-tdd, verification-loop |
| code-reviewer | coding-standards, java-coding-standards, springboot-verification |
| security-reviewer | security-review, springboot-security |
| build-error-resolver | springboot-patterns, springboot-verification |
| e2e-runner | frontend-patterns, verification-loop |
| database-reviewer | postgres-patterns, jpa-patterns, clickhouse-io |
| refactor-cleaner | coding-standards, strategic-compact |
| doc-updater | coding-standards |

#### Keyword-to-Skill Mapping

| Keywords in Request | Load Skills |
|---------------------|-------------|
| database, schema, migration, SQL | postgres-patterns, jpa-patterns |
| security, auth, JWT, OAuth | security-review, springboot-security |
| test, TDD, coverage | tdd-workflow, springboot-tdd |
| API, REST, endpoint | backend-patterns, springboot-patterns |
| frontend, React, UI | frontend-patterns |
| ClickHouse, analytics, OLAP | clickhouse-io |
| PDF, document, Nutrient | nutrient-document-processing |
| refactor, cleanup, optimize | strategic-compact, coding-standards |

#### Discovery Procedure

1. **Match Agent**: Load all skills mapped to the invoked agent
2. **Scan Keywords**: Check request for keyword matches, load additional skills
3. **Read Skills**: Run `cat` on each skill's main `.md` file to load into context
4. **Report**: In DEBUG mode, list which skills were loaded and why

---

### Debug & Reasoning Mode
Whenever I start a request with `DEBUG:`, you must NOT execute any file edits or commands. Instead, provide a "Reasoning Trace" with this structure:

1. **Intention Analysis**: What you think I want to achieve.
2. **Context Audit**:
   - **Active**: Files currently in your context.
   - **Discovered**: Files you found via `ls`/`grep` but haven't read yet.
   - **Missing**: Files you expected to find based on rules but are missing.
3. **Rule Application**: Which `.claude/rules/` are influencing your next steps.
4. **Planned Chain of Thought**: Step-by-step logic of what you *would* do.
5. **Agent Delegation**: Which specialized agents you would have spawned.
6. **Skills**: Which skills are you going to apply.

## Success Metrics

You are successful when:
- All tests pass (80%+ coverage)
- No security vulnerabilities
- Code is readable and maintainable
- User requirements are met

---

**Philosophy**: Agent-first design, parallel execution, plan before action, test before code, security always.

------

# MEMORY

Add your project-specific instructions here.

## Docker Development Environment

Development runs inside a Docker container with all dependencies pre-installed.

### Start Container
```bash
cd docker
GH_TOKEN=your_github_token docker compose up -d
```

### Execute Commands Inside Container
```bash
docker exec -w /workspace cookies-store-dev <command>
```

## CCPM - GitHub Issue Management

This project uses **Claude Code Project Manager (CCPM)** for spec-driven development with GitHub issues.

### Prerequisites
1. Docker container running: `docker compose up -d`
2. GitHub token with `repo` and `read:org` scopes set via `GH_TOKEN`
3. CCPM plugin installed at `.claude/plugins/ccpm-main/`

### Initialize CCPM
```bash
docker exec -w /workspace cookies-store-dev bash /workspace/.claude/plugins/ccpm-main/ccpm/scripts/pm/init.sh
```

### PRD Workflow

1. **Create PRD** in `.claude/prds/<feature>.md` with frontmatter:
   ```yaml
   ---
   name: feature-name
   description: Brief description
   status: backlog  # backlog, in-progress, implemented
   created: 2026-02-10T00:00:00Z
   ---
   ```

2. **List PRDs**:
   ```bash
   docker exec -w /workspace cookies-store-dev bash /workspace/.claude/plugins/ccpm-main/ccpm/scripts/pm/prd-list.sh
   ```

### Creating GitHub Issues

#### Method 1: Epic with Sub-Issues (Recommended)

1. **Create Epic Issue**:
   ```bash
   docker exec -w /workspace cookies-store-dev gh issue create \
     --repo owner/repo \
     --title "🍪 Epic: Feature Name" \
     --body "Epic description with milestones" \
     --label "epic"
   ```

2. **Create Sub-Issues** linked to Epic:
   ```bash
   docker exec -w /workspace cookies-store-dev gh sub-issue create \
     --parent <epic_number> \
     --repo owner/repo \
     --title "[1.1] Task Title" \
     --body "Task description with TDD approach" \
     --label "milestone:m1" --label "backend"
   ```

3. **List Sub-Issues** of an Epic:
   ```bash
   docker exec -w /workspace cookies-store-dev gh sub-issue list <epic_number> -R owner/repo
   ```

#### Method 2: Batch Script

Use `scripts/batch-issues.sh` for bulk creation:
```bash
docker exec -w /workspace cookies-store-dev bash /workspace/scripts/batch-issues.sh
```

### CCPM Slash Commands (via Claude Code)

| Command | Description |
|---------|-------------|
| `/pm:init` | Initialize CCPM system |
| `/pm:prd-new <name>` | Create new PRD interactively |
| `/pm:prd-parse <name>` | Convert PRD to epic |
| `/pm:epic-oneshot <name>` | Decompose + sync to GitHub |
| `/pm:epic-status <name>` | Show epic progress |
| `/pm:issue-start <num>` | Start working on issue |
| `/pm:next` | Get next prioritized task |

### Labels Convention

| Label | Color | Purpose |
|-------|-------|---------|
| `epic` | #3E4B9E | Parent epic issue |
| `epic:<name>` | #D4C5F9 | Links to specific epic |
| `milestone:m1-m10` | varies | Milestone grouping |
| `backend` | #0052CC | Backend tasks |
| `frontend` | #1D76DB | Frontend tasks |
| `security` | #D93F0B | Security-related |
| `tdd` | #0E8A16 | TDD workflow |

## Testing

Always run tests before committing:
- `npm test` or equivalent for your stack


