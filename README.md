# Cookies Store - E-Commerce Platform

Multi-module Spring Boot application for an e-commerce platform.

## Project Structure

```
cookies-store/
├── build.gradle.kts          # Root build configuration
├── settings.gradle.kts        # Module declarations
├── gradle.properties          # Build properties
├── gradle/
│   └── libs.versions.toml    # Version catalog
├── common/                    # Shared utilities
├── catalog-module/            # Product catalog
├── cart-module/              # Shopping cart
├── customer-module/          # Customer management
├── order-module/             # Order processing
├── payment-module/           # Payment processing
├── admin-module/             # Admin functionality
└── application/              # Main application (aggregates all modules)
```

`application` acts as the composition root (runtime entrypoint).  
Admin controllers, templates, and admin services belong in `admin-module`.

## Module Dependencies

```
application
├── common
├── catalog-module → common
├── cart-module → common
├── customer-module → common
├── order-module → common
├── payment-module → common
└── admin-module → common
```

## Requirements

- Java 21+
- Gradle 8.5+

## Setup

1. Initialize Gradle wrapper:
```bash
# If gradle is installed
gradle wrapper --gradle-version 8.5

# Or download wrapper manually
mkdir -p gradle/wrapper
curl -L https://services.gradle.org/distributions/gradle-8.5-bin.zip -o gradle-8.5-bin.zip
unzip gradle-8.5-bin.zip
gradle-8.5/bin/gradle wrapper
rm -rf gradle-8.5 gradle-8.5-bin.zip
```

2. Build project:
```bash
./gradlew build
```

## Build Commands

```bash
# Build all modules
./gradlew build

# Build specific module
./gradlew :catalog-module:build

# Run tests
./gradlew test

# Run tests for specific module
./gradlew :catalog-module:test

# Clean build
./gradlew clean build

# Check dependencies
./gradlew dependencies
```

## Development

Each module follows standard Spring Boot structure:
```
module/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
│       ├── java/
│       └── resources/
└── build.gradle.kts
```

## Testing

### Test Infrastructure

The project uses:
- **JUnit 5** for unit and integration testing
- **jqwik** for property-based testing
- **TestContainers** for integration tests with PostgreSQL and Redis

### Running Tests

```bash
# Run all tests
./gradlew test

# Run tests for a specific module
./gradlew :common:test
./gradlew :catalog-module:test

# Run specific test class
./gradlew :common:test --tests "*PostgresContainerTest*"

# Run with verbose output
./gradlew test --info
```

### Integration Tests with TestContainers

Tests extending `AbstractIntegrationTest` use TestContainers to spin up PostgreSQL and Redis automatically. These tests require Docker to be available.

```bash
# Run TestContainers-based tests (requires Docker)
./gradlew :common:test --tests "*ContainerTest*"
```

> **Note:** Tests using TestContainers are annotated with `@RequiresDocker` and will be skipped gracefully if Docker is not available.

### Integration Tests with External Containers (DevContainer)

When running inside a devcontainer where Docker CLI is not available, use the external container tests that connect to pre-existing Docker Compose services.

1. **Start the Docker Compose services** (from host machine):
```bash
cd docker
docker-compose up -d
```

2. **Create the database** (if not exists):
```bash
docker exec -it cookies-store-postgres psql -U core -c "CREATE DATABASE cookies_store;"
```

3. **Run external container tests**:
```bash
./gradlew :common:test --tests "*External*"
```

The external tests connect to:
- **PostgreSQL**: `172.18.0.1:5432` (Docker gateway IP)
- **Redis**: `172.18.0.1:6379`

### Test Profiles

| Profile | Description | Use Case |
|---------|-------------|----------|
| `test` | Uses TestContainers | Local dev with Docker |
| `external` | Uses external containers | DevContainer environment |

## Next Steps

1. Add domain entities to each module
2. Implement repository layer
3. Create service layer
4. Build REST controllers
5. Add security configuration
6. Set up database migrations

## CI/CD

All modules must:
- Pass compilation
- Pass all tests
- Meet 80% code coverage
- Pass security scans
