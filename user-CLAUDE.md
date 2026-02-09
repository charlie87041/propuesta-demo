

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

Detailed guidelines are in `~/.claude/rules/`:

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

Located in `~/.claude/agents/`:

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

## Editor Integration

I use Zed as my primary editor:
- Agent Panel for file tracking
- CMD+Shift+R for command palette
- Vim mode enabled

---

## 🛠️ Activation & Orchestration Logic

To maintain the **Agent-First** philosophy, you must follow these triggers to decide when to use a specific skill or agent:

### 1. Agent Trigger Matrix
| If the task is... | Invoke Agent(@agents)... | Load Rule Context(@rules/common)... | Context(@contexts)...
| :--- | :--- | :--- |
| **Starting a new feature** | `planner-tdd` | `patterns.md`, `testing.md` | `planner-tdd.md`  | 
| **Designing API/System** | `architect` | `patterns.md` | `research.md`  | 
| **Implementing logic** | `tdd-guide` | `coding-style.md`, `testing.md` |`dev.md`  | 
| **Fixing a broken build** | `build-error-resolver`| `performance.md` |`dev.md`  | 
| **Finalizing a PR** | `code-reviewer` | `git-workflow.md`, `coding-style.md` |`review.md`  | 
| **Handling Auth/Data** | `security-reviewer` | `security.md` |`review.md`  | 
| **UI/Flow validation** | `e2e-runner` | `hooks.md` |

### 2. Skill Execution Rules
* **Discovery Skill**: Always use `ls`, `grep`, and `find` to map the project before proposing changes. Do not ask for file locations.
* **Planning Skill**: For any task involving >3 files, you **must** use `Plan Mode` and write the plan to a temporary `TODO.md` before executing.
* **Parallelism**: If a task requires writing tests and implementing logic simultaneously, use the `Task` tool to spawn `tdd-guide` and `code-reviewer` in parallel.
* **Verification Skill**: After any implementation, automatically run the relevant test suite defined in `testing.md`. Do not wait for user confirmation to verify your own code.

### 3. Context Loading Protocol
* **Pre-flight**: Before the first edit of any session, run `cat` on `.claude/rules/coding-style.md` to refresh active memory.
* **Conflict Resolution**: If a requirement in a specialized rule (e.g., `security.md`) conflicts with a general preference, the **specialized rule** always takes precedence.

---

### 🛡️ Debug & Reasoning Mode
Whenever I start a request with `DEBUG:`, you must NOT execute any file edits or commands. Instead, provide a "Reasoning Trace" with this structure:

1. **Intention Analysis**: What you think I want to achieve.
2. **Context Audit**:
   - ✅ **Active**: Files currently in your context.
   - 🔍 **Discovered**: Files you found via `ls`/`grep` but haven't read yet.
   - ❌ **Ignored/Missing**: Files you expected to find based on rules but are missing.
3. **Rule Application**: Which `.claude/rules/` are influencing your next steps.
4. **Planned Chain of Thought**: Step-by-step logic of what you *would* do.
5. **Agent Delegation**: Which specialized agents you would have spawned.
5. **Skills**: Which skills are you going to apply.

## 🧠 Reasoning & Justification Standards (The "Why")

When in **DEBUG** or **PLAN** mode, or whenever I ask "Why?", you must provide a **Decision Log** using the following criteria:

### 1. Context Selection Logic
For every file included or excluded, justify:
- **Inclusion**: "I read `X.md` because the keyword 'Auth' triggered the `security.md` rule."
- **Exclusion**: "I ignored `Y.ts` because its last modified date or size suggested it was irrelevant to the current logic, prioritizing token efficiency."

### 2. Rule-Based Justification
When you propose a code change, link it to a specific rule:
- *Example*: "I am choosing a `const` with `...spread` instead of `push()` **because** `coding-style.md` mandates immutability."

### 3. Agent Delegation Logic
Explain why a specific agent was chosen over doing it yourself:
- *Example*: "I'm delegating to `build-error-resolver` because the error trace is >50 lines, and its specialized skill for log analysis is more efficient than my current context window."

### 4. Alternative Consideration
Briefly mention what you **didn't** do:
- "I considered using a Class, but rejected it to stay consistent with the Functional Core principle in `patterns.md`."

## Success Metrics

You are successful when:
- All tests pass (80%+ coverage)
- No security vulnerabilities
- Code is readable and maintainable
- User requirements are met

---

**Philosophy**: Agent-first design, parallel execution, plan before action, test before code, security always.
