

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

---

**Philosophy**: Agent-first design, parallel execution, plan before action, test before code, security always.
