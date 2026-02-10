# Agent Authorization Integration

**Extracted:** 2026-02-10
**Context:** When planning/architecture agents generate incomplete designs missing authorization

## Problem

Planning agents (architect, planner, planner-tdd) may overlook authorization requirements,
generating designs and tasks that lack:
- Permission checks per endpoint
- Ownership validation
- Domain/tenant isolation
- Authorization test cases

This results in insecure implementations that require rework.

## Solution

Update agent definitions to systematically consider authorization:

### 1. Add to Architect Agent

In the System Design Checklist, add:
- [ ] Domains defined (tenants, bounded contexts)
- [ ] Abilities defined with permission groupings
- [ ] Permissions follow `{resource}:{action}` naming
- [ ] Per-endpoint permission requirements specified

Add Authorization Principle section:
- Domain → Ability → Permission hierarchy
- Deny by default
- Domain isolation for multi-tenancy
- Ownership + permission combined checks

### 2. Add to Planner Agent

In Requirements Analysis, add:
- What domains apply?
- What abilities are needed?
- What permissions per endpoint?
- Who owns resources? (ownership validation)

Add Red Flags:
- Missing authorization checks
- Missing ownership validation
- Insecure direct object references

### 3. Add to Planner-TDD Agent

In Issue Template, add authorization tests:
- [ ] Unauthenticated access returns 401
- [ ] Missing permission returns 403
- [ ] Wrong domain returns 403
- [ ] Non-owner access returns 403
- [ ] Granted permission succeeds
- [ ] Explicit override (deny) returns 403

In Milestone Organization, make authorization explicit:
- Authorization module with entities, service, annotations
- Permission seed data
- Ownership validation utilities

In Input Analysis, identify:
- Domains (tenants, organizational units)
- Abilities (permission bundles)
- Permissions (atomic actions per endpoint)
- Resource ownership rules

### 4. Create/Update Security Skill

Document the authorization model in a skill file that agents can reference:

```
skills/springboot-security/domain-ability-authorization.md
```

Include:
- Data model (Domain, Ability, Permission, UserDomainAbility entities)
- Authorization service implementation
- Custom annotations (@RequiresPermission, @RequiresAbility)
- Spring Security integration
- Test patterns (unit + integration)
- Seed data structure

## Example Changes

### Before (Agent without authorization)
```markdown
### 1. Requirements Analysis
- Understand the feature request completely
- Identify success criteria
- List assumptions and constraints
```

### After (Agent with authorization)
```markdown
### 1. Requirements Analysis
- Understand the feature request completely
- Identify success criteria
- List assumptions and constraints
- **Identify authorization requirements**:
  - What domains apply?
  - What abilities are needed?
  - What permissions per endpoint?
  - Who owns resources?
```

## When to Use

- When creating new planning/architecture agents
- When reviewing existing agents for security gaps
- When authorization is missing from generated plans
- When onboarding a project requiring multi-tenant security
- When implementing Domain-Ability-Permission model

## Related Skills

- `springboot-security` - Implementation patterns
- `springboot-security/domain-ability-authorization.md` - Full authorization model

## Verification

After applying this pattern, generated plans should include:
- [ ] Authorization module in architecture
- [ ] Permission annotations on controller methods
- [ ] Authorization tests in each controller task
- [ ] Ownership validation where applicable
- [ ] Domain isolation for admin endpoints
