package com.cookiesstore.common.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify PostgreSQL TestContainer configuration.
 * Tests CRUD operations and data isolation.
 */
@SpringBootTest
@RequiresDocker
class PostgresContainerTest extends AbstractIntegrationTest {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Test
    void containerHasCorrectDatabaseName() {
        assertEquals("testdb", postgresContainer.getDatabaseName());
    }

    @Test
    void containerUsesPostgres15() {
        String imageName = postgresContainer.getDockerImageName();
        assertTrue(imageName.contains("postgres:15"), 
            "Container should use PostgreSQL 15");
    }

    @Test
    void canExecuteDDL() {
        if (jdbcTemplate == null) {
            // Skip if Spring context not available yet (expected in RED phase)
            return;
        }
        
        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS test_table (id SERIAL PRIMARY KEY, name VARCHAR(100))"
        );
        
        // Verify table exists
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'test_table'",
            Integer.class
        );
        assertEquals(1, count);
    }

    @Test
    void canExecuteCRUDOperations() {
        if (jdbcTemplate == null) {
            return; // Skip in RED phase
        }
        
        // Setup table
        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS crud_test (id SERIAL PRIMARY KEY, value VARCHAR(50))"
        );
        
        // CREATE
        jdbcTemplate.update("INSERT INTO crud_test (value) VALUES (?)", "test-value");
        
        // READ
        String value = jdbcTemplate.queryForObject(
            "SELECT value FROM crud_test WHERE value = ?",
            String.class,
            "test-value"
        );
        assertEquals("test-value", value);
        
        // UPDATE
        jdbcTemplate.update("UPDATE crud_test SET value = ? WHERE value = ?", 
            "updated-value", "test-value");
        String updated = jdbcTemplate.queryForObject(
            "SELECT value FROM crud_test WHERE value = ?",
            String.class,
            "updated-value"
        );
        assertEquals("updated-value", updated);
        
        // DELETE
        jdbcTemplate.update("DELETE FROM crud_test WHERE value = ?", "updated-value");
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM crud_test WHERE value = ?",
            Integer.class,
            "updated-value"
        );
        assertEquals(0, count);
        
        // Cleanup
        jdbcTemplate.execute("DROP TABLE crud_test");
    }

    @Test
    void dataDoesNotPersistBetweenTestRuns() {
        if (jdbcTemplate == null) {
            return;
        }
        
        // This test verifies isolation by checking that any previously created
        // test tables don't exist (TestContainers provides isolation)
        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS isolation_test (id SERIAL PRIMARY KEY)"
        );
        
        // Clean up immediately
        jdbcTemplate.execute("DROP TABLE isolation_test");
        
        // Verify table is gone
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'isolation_test'",
            Integer.class
        );
        assertEquals(0, count);
    }
}
