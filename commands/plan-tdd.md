---
name: plan-tdd
description: Create TDD-first implementation plan with property-based testing and auto-generate GitHub issues via CCPM
---

# TDD Implementation Planning + CCPM Integration

This command creates a complete implementation plan following TDD principles and automatically generates GitHub issues.

## Usage

```bash
# Step 1: Create TDD implementation plan
/plan-tdd <feature-description>

# Step 2: Auto-generate issues in GitHub (via CCPM)
/pm:prd-new <feature-name>
/pm:prd-parse <feature-name>
/pm:epic-oneshot <feature-name>
```

## Workflow

### Phase 1: TDD Plan Generation

When you run `/plan-tdd <description>`:

1. **Loads Context**: Automatically includes `planner-tdd.md` context with template references

2. **Delegate to planner-tdd agent**:
   ```
   @planner-tdd create implementation plan for: <description>
   ```

3. **Agent will analyze**:
   - Identify entities, services, APIs
   - Extract correctness properties from design docs
   - Organize into milestones (Infrastructure → Core → Domain → Advanced)
   - Break into granular issues with TDD structure

4. **Agent will output**:
   - Complete milestone breakdown
   - Issues with Red-Green-Refactor phases
   - Property test mappings
   - Documentation tasks per issue
   - Acceptance criteria
   - Estimates

### Phase 2: PRD Creation for CCPM

Convert the plan into a Product Requirements Document (PRD):

1. **Create PRD file** (`prds/<feature-name>.md`):
   ```markdown
   # <Feature Name> PRD
   
   ## Overview
   [High-level description from plan]
   
   ## Milestones
   
   ### Milestone 1: <Name>
   **Duration**: <estimate>
   **Goal**: <milestone goal>
   
   #### Issues
   
   ##### Issue 1.1: <Title>
   **Labels**: enhancement, backend, testing
   **Estimate**: <days>
   
   **Description**:
   <Copy from planner-tdd output>
   
   **TDD Approach**:
   - Red Phase: <tests>
   - Green Phase: <implementation>
   - Refactor Phase: <improvements + docs>
   
   **Acceptance Criteria**:
   - [ ] All tests pass
   - [ ] Coverage > 80%
   - [ ] Documentation complete
   
   [Repeat for all issues...]
   ```

2. **Generate issues**:
   ```bash
   /pm:prd-new <feature-name>
   /pm:prd-parse <feature-name>
   /pm:epic-oneshot <feature-name>
   ```

### Phase 3: Implementation

For each issue:

```bash
# Start working on an issue
/pm:issue-start <issue-number>

# Follow TDD cycle
/tdd  # Use existing TDD command

# Document as you go
# (inline docs during Refactor phase)

# Complete issue
/pm:issue-complete <issue-number>
```

## Example

```bash
# 1. Generate TDD plan
/plan-tdd "User authentication system with JWT tokens, password reset, and email verification"

# Output will include:
# - Milestone 1: Infrastructure (TestContainers, JWT setup)
# - Milestone 2: User Management (Entity, Repository, Service, Controller)
# - Milestone 3: Authentication (Login, Password reset, Email verification)
# Each with detailed TDD tasks

# 2. Create PRD from plan
# (manually copy plan to prds/user-auth.md)

# 3. Generate GitHub issues
/pm:prd-new user-auth
/pm:prd-parse user-auth
/pm:epic-oneshot user-auth

# 4. Start implementing
/pm:issue-start 1  # First issue
/tdd               # Follow TDD workflow
```

## Key Benefits

✅ **TDD-First**: Every task includes Red-Green-Refactor phases
✅ **Property-Based Testing**: Maps design properties to property tests
✅ **Documentation Integrated**: Not a separate phase, part of every task
✅ **GitHub Integration**: Auto-creates issues via CCPM
✅ **Progress Tracking**: CCPM tracks issue status and progress
✅ **Dependency Management**: Clear milestone dependencies
✅ **Quality Gates**: Explicit acceptance criteria per issue

## Configuration

### 1. Install planner-tdd agent

```bash
# Copy to your agents directory
cp planner-tdd.md ~/.claude/agents/
```

### 2. Install TDD skill

```bash
# Copy skill to your skills directory
cp tdd-milestone-planning ~/.claude/skills/
```

### 3. Install CCPM

```bash
curl -sSL https://automaze.io/ccpm/install | bash
```

### 4. Configure GitHub

CCPM requires GitHub token:
```bash
export GITHUB_TOKEN=<your-token>
```

## Templates

### Issue Template for CCPM

When creating the PRD, use this format for each issue:

```markdown
##### Issue X.Y: <Title>

**Labels**: enhancement, backend, testing
**Estimate**: <days>

**Description**:
<What and Why>

**Related Requirements**:
- Requirement X.Y

**Related Properties** (if applicable):
- Property N: <description>

**TDD Approach**:

Red Phase:
- [ ] Unit tests: <scenarios>
- [ ] Integration tests: <scenarios>
- [ ] Property tests: Property N (100+ iterations)

Green Phase:
- [ ] Implement <component>
- [ ] <steps>

Refactor Phase:
- [ ] Extract duplications
- [ ] Add logging
- [ ] Write Javadoc/JSDoc
- [ ] Update API docs

**Acceptance Criteria**:
- [ ] All tests pass
- [ ] Coverage > 80%
- [ ] No warnings
- [ ] Documentation complete
- [ ] Code review approved

**Dependencies**:
- Requires: Issue X.Y
```

## Tips

1. **Property Tests**: If your design has correctness properties, make sure they're documented. The planner will map them to property tests.

2. **Milestone Completion**: Add a final task to each milestone for architecture documentation.

3. **Documentation**: Never create a separate documentation milestone. Docs are part of every task's Refactor phase.

4. **Granularity**: Keep tasks 1-4 days. Split larger ones.

5. **Coverage**: 80% minimum is non-negotiable in acceptance criteria.

6. **CCPM Sync**: CCPM will update issue status automatically as you work.

## Anti-Patterns

❌ Don't skip property tests when properties exist
❌ Don't create tasks without TDD phases
❌ Don't leave documentation for the end
❌ Don't mix multiple entities in one task
❌ Don't create tasks larger than 4 days
❌ Don't ignore acceptance criteria

## Success Metrics

- ✅ 100% of tasks follow Red-Green-Refactor
- ✅ All properties have property tests (100+ iterations)
- ✅ >80% code coverage on all modules
- ✅ All public methods have documentation
- ✅ All endpoints have OpenAPI docs
- ✅ 0 broken tests in main branch
- ✅ Documentation updated per milestone