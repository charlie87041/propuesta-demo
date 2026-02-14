package com.cookiesstore.common.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base class for integration tests with PostgreSQL and Redis TestContainers.
 * Provides shared containers for all integration tests.
 * 
 * Usage:
 * <pre>
 * {@code
 * @SpringBootTest
 * class MyRepositoryTest extends AbstractIntegrationTest {
 *     @Test
 *     void testDatabaseOperation() {
 *         // Test code here
 *     }
 * }
 * }
 * </pre>
 */
@Testcontainers
@RequiresDocker
public abstract class AbstractIntegrationTest {

    private static final int REDIS_PORT = 6379;

    @Container
    static final PostgreSQLContainer<?> postgresContainer = 
        new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @Container
    static final GenericContainer<?> redisContainer =
        new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(REDIS_PORT)
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL configuration
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        
        // Redis configuration
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(REDIS_PORT));
    }

    @Test
    void postgresContainerIsRunning() {
        assertTrue(postgresContainer.isRunning(), "PostgreSQL container should be running");
    }

    @Test
    void canConnectToDatabase() {
        assertNotNull(postgresContainer.getJdbcUrl(), "JDBC URL should be available");
        assertTrue(postgresContainer.getJdbcUrl().contains("jdbc:postgresql"), 
            "JDBC URL should be a PostgreSQL connection");
    }
}
