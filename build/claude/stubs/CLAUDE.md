

## Project Overview

This system implements a collaborative document management platform using Spring Boot with a multi-module architecture managed by Gradle.

## Critical Rules

### 1. Code Organization

- Many small files over few large files
- High cohesion, low coupling
- 200-400 lines typical, 800 max per file
- Organize by feature/domain, not by type

### 2. Code Style

- No emojis in code, comments, or documentation
- Immutability always - prefer records and final fields
- No System.out.println in production code (use SLF4J/Logback)
- Proper exception handling with domain-specific exceptions
- Input validation with Bean Validation (JSR-380): `@NotNull`, `@NotBlank`, `@Valid`

### 3. Testing

- TDD: Write tests first
- 80% minimum coverage
- Unit tests for utilities
- Integration tests for APIs
- E2E tests for critical flows

### 4. Security

- No hardcoded secrets
- Environment variables for sensitive data
- Validate all user inputs
- Parameterized queries only
- CSRF protection enabled

## File Structure

Follow the standard Spring Boot layered architecture:

```
src/main/java/com/example/app/
  config/       # Configuration classes
  controller/   # REST controllers
  service/      # Business logic
  repository/   # Data access (JPA)
  domain/       # Entities and value objects
  dto/          # Request/Response DTOs
  exception/    # Custom exceptions
```

**Skills**: For detailed patterns, load `springboot-patterns` and `java-coding-standards` skills.

## Key Patterns

### API Response Format

```json
{
  "success": true,
  "data": { },
  "error": null
}
```

## Environment Variables

```bash
# Required
DATABASE_URL=
API_KEY=

# Optional
DEBUG=false
```

## Available Commands

- `/tdd` - Test-driven development workflow
- `/plan-tdd` - Create implementation plan
- `/code-review` - Review code quality
- `/build-fix` - Fix build errors

## Git Workflow

- Conventional commits: `feat:`, `fix:`, `refactor:`, `docs:`, `test:`
- Never commit to main directly
- PRs require review
- All tests must pass before merge

## IMPORTANT

user-CLAUDE.md gathers more grained information you MUST keep in memory.