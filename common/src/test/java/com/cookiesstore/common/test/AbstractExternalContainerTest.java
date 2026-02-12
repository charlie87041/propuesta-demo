package com.cookiesstore.common.test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Base class for integration tests using external containers (devcontainer environment).
 * Uses the 'external' profile which connects to postgres and redis containers
 * defined in docker-compose.yml.
 * 
 * Usage:
 * <pre>
 * {@code
 * class MyRepositoryTest extends AbstractExternalContainerTest {
 *     @Test
 *     void testDatabaseOperation() {
 *         // Test code here - connects to external postgres/redis
 *     }
 * }
 * }
 * </pre>
 * 
 * Run with: ./gradlew test -Dspring.profiles.active=external
 */
@SpringBootTest
@ActiveProfiles("external")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://172.18.0.1:5432/cookies_store",
    "spring.datasource.username=core",
    "spring.datasource.password=secret",
    "spring.data.redis.host=172.18.0.1",
    "spring.data.redis.port=6379"
})
public abstract class AbstractExternalContainerTest {
    // Base class for tests using external containers
    // No TestContainers setup needed - uses docker-compose services
}
