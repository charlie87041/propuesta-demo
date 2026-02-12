# Integration Testing with TestContainers

This module provides PostgreSQL TestContainers configuration for integration testing.

## Quick Start

### 1. Extend AbstractIntegrationTest

```java
@SpringBootTest
class MyRepositoryTest extends AbstractIntegrationTest {
    
    @Autowired
    private MyRepository repository;
    
    @Test
    void testSaveAndFind() {
        MyEntity entity = new MyEntity("test");
        repository.save(entity);
        
        Optional<MyEntity> found = repository.findById(entity.getId());
        assertTrue(found.isPresent());
    }
}
```

### 2. Container Configuration

The PostgreSQL container is configured with:
- **Image**: `postgres:15-alpine`
- **Database**: `testdb`
- **Username**: `test`
- **Password**: `test`
- **Reuse**: Enabled for faster test execution

### 3. Running Tests

```bash
# Run all tests
./gradlew test

# Run only integration tests
./gradlew test --tests "*IntegrationTest"

# Run with container logs
./gradlew test -Dorg.testcontainers.utility.logging=DEBUG
```

## Features

### Container Reuse

Containers are reused across test runs for faster execution:
- First run: ~10-15 seconds (container startup)
- Subsequent runs: ~2-3 seconds (reuses existing container)

To disable reuse:
```java
postgresContainer.withReuse(false);
```

### Test Isolation

Each test class gets a fresh Spring context, ensuring:
- No data leakage between test classes
- Schema changes don't affect other tests
- Parallel test execution is safe

### Property-Based Testing

Use `PostgresContainerPropertyTest` as a template for property tests:

```java
@Property(tries = 100)
void roundTripProperty(
    @ForAll @IntRange(min = 1, max = 1000) int id,
    @ForAll String value
) {
    // Write
    repository.save(new Entity(id, value));
    
    // Read
    Entity found = repository.findById(id).orElseThrow();
    
    // Assert equality
    assertEquals(value, found.getValue());
}
```

## Troubleshooting

### Docker Not Running

```
Error: Could not start container
```

**Solution**: Start Docker Desktop or Docker daemon

### Port Conflicts

```
Error: Port 5432 already in use
```

**Solution**: TestContainers uses random ports automatically. Check for:
- Existing PostgreSQL instances
- Previous test containers not cleaned up

### Slow Tests

**Solutions**:
1. Enable container reuse (already enabled by default)
2. Use `@DirtiesContext` sparingly
3. Run tests in parallel: `./gradlew test --parallel`

## Best Practices

1. **Always extend AbstractIntegrationTest**
   - Provides shared container configuration
   - Reduces container startup overhead

2. **Clean up test data**
   - Use `@AfterEach` for cleanup when needed
   - Avoid relying on automatic rollback for integration tests

3. **Use meaningful test data**
   - Use builders or factories for complex entities
   - Keep test data minimal and focused

4. **Test real scenarios**
   - Test actual SQL queries, not mocked behavior
   - Include edge cases (nulls, empty strings, special characters)

5. **Monitor container resources**
   - Containers are automatically cleaned up
   - Check Docker for orphaned containers if tests fail

## Examples

See:
- `PostgresContainerTest.java` - Basic integration tests
- `PostgresContainerPropertyTest.java` - Property-based tests
