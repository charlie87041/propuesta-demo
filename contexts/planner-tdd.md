# TDD Planning Context

Mode: Specification and implementation planning
Focus: Test-Driven Development with property-based testing

## Behavior
- Read reference templates before creating specs
- Follow Red-Green-Refactor cycle for every task
- Map correctness properties to property-based tests
- Organize by architectural dependencies
- Document as you go (not as final phase)

## Reference Templates

**IMPORTANT**: Always consult these templates for structure and format:

### Requirements Template
Path: `@artifacts/specs/requirements.example.md`

Key elements to replicate:
- Introduction with system overview
- Glossary of domain terms
- User stories with "As a [role], I want [goal], so that [benefit]"
- Acceptance criteria in WHEN/THEN format
- Numbered requirements for traceability

Example structure:
```markdown
### Requirement N: [Title]

**User Story:** As a [role], I want [goal], so that [benefit].

#### Acceptance Criteria
1. WHEN [condition], THE System SHALL [behavior]
2. WHEN [condition], THE System SHALL [behavior]
```

### Design Template
Path: `@artifacts/specs/design.example.md`

Key elements to replicate:
- High-level architecture with diagrams (Mermaid)
- Technology stack specification
- Component interfaces (Controllers, Services, Repositories)
- Data models with JPA annotations
- **Correctness Properties** section

Correctness Properties format:
```markdown
**Property N: [Descriptive name]**
*For any* [conditions], [the system] should [expected behavior].
**Validates: Requirements X.Y**
```

### Tasks Template
Path: `@artifacts/specs/tasks@example.md`

Key elements to replicate:
- Milestone organization (Infrastructure → Core → Advanced)
- Each task with TDD phases:
  - **Red Phase**: Write failing tests (unit, integration, property)
  - **Green Phase**: Minimum implementation
  - **Refactor Phase**: Improve + document
- Property tests linked to design properties
- Acceptance criteria with checkboxes
- Time estimates (0.5-4 days max per task)

Example task structure:
```markdown
### Task X.Y: [Title]

**Descripción**: [What and why]

**Enfoque TDD**:
1. **Red Phase - Write Failing Tests**:
   - Unit Tests: [scenarios]
   - Integration Tests: [scenarios]
   - Property Tests: [properties to validate]

2. **Green Phase - Implement Minimum Code**:
   - [Implementation steps]

3. **Refactor Phase**:
   - [Improvements + documentation]

**Acceptance Criteria**:
- [ ] All tests pass
- [ ] Property tests pass (100+ iterations)
- [ ] Coverage > 80%

**Related Requirements**: [List]
**Related Properties**: [List]
```

## Planning Process

When creating implementation plans:

1. **Read Templates First**
   - Open and review all three template files
   - Understand the format, structure, and level of detail
   - Note the style of correctness properties

2. **Analyze Requirements**
   - Identify entities, services, APIs
   - Extract business rules
   - Define correctness properties

3. **Design Architecture**
   - Follow dependency order (Infrastructure → Shared → Core → Domain → Advanced)
   - Define component interfaces
   - Map properties to components

4. **Create Task Breakdown**
   - One task = one entity/component/feature
   - Max 4 days per task
   - Always include Red-Green-Refactor phases
   - Link properties to property tests

5. **Validate Completeness**
   - Every requirement has tasks
   - Every property has tests
   - Dependencies are clear
   - Estimates are realistic

## Quality Gates

**For Requirements**:
- [ ] All user stories have acceptance criteria
- [ ] Acceptance criteria use WHEN/THEN format
- [ ] Requirements are numbered and traceable
- [ ] Glossary defines all domain terms

**For Design**:
- [ ] Architecture diagram included
- [ ] All components have interface definitions
- [ ] Data models include JPA annotations
- [ ] Correctness properties defined
- [ ] Properties linked to requirements

**For Tasks**:
- [ ] Organized by milestones
- [ ] Each task has Red-Green-Refactor phases
- [ ] Property tests reference design properties
- [ ] Acceptance criteria are measurable
- [ ] No task exceeds 4 days
- [ ] Dependencies clearly stated

## Anti-Patterns to Avoid

- ❌ Creating specs without reading templates
- ❌ Skipping property-based tests
- ❌ Mixing multiple entities in one task
- ❌ Leaving documentation for final phase
- ❌ Creating tasks without clear acceptance criteria
- ❌ Ignoring architectural dependencies

## Tools to Favor

- Read for reviewing template files
- Grep for finding patterns in templates
- Write for creating spec documents
- Task with planner-tdd agent for complex planning

## Usage Examples

### With planner-tdd agent:
```
@planner-tdd #planner-tdd.md

Create an implementation plan for user authentication with JWT tokens.
Follow the templates in @artifacts/specs/ for structure.
```

### Direct planning:
```
#planner-tdd.md

I need to create a spec for a REST API with CRUD operations.
Use the reference templates for format.
```

### With existing requirements:
```
@planner-tdd #planner-tdd.md

I have requirements in requirements.md. Create the design and tasks documents
following the template structure in @artifacts/specs/.
```

## Output Format

When creating specs, produce three documents:

1. **requirements.md**: User stories + acceptance criteria
2. **design.md**: Architecture + components + properties
3. **tasks.md**: Milestones + TDD tasks + estimates

Each document should match the structure and detail level of the templates.

## Notes for Claude Code Compatibility

- Use relative paths from workspace root
- Reference files with backticks: `@artifacts/specs/requirements.example.md`
- Keep markdown formatting consistent
- Use standard markdown checkboxes: `- [ ]`
- Include code blocks with language tags
- Use Mermaid for diagrams when applicable
