package com.cookiesstore.common.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test using external PostgreSQL container.
 * Run with: ./gradlew test --tests "*ExternalPostgresTest" -Dspring.profiles.active=external
 */
class ExternalPostgresTest extends AbstractExternalContainerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void canConnectToExternalPostgres() {
        assertNotNull(jdbcTemplate, "JdbcTemplate should be autowired");
        
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        assertEquals(1, result, "Should be able to execute simple query");
    }

    @Test
    void canExecuteDDL() {
        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS external_test_table (id SERIAL PRIMARY KEY, name VARCHAR(100))"
        );
        
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'external_test_table'",
            Integer.class
        );
        assertEquals(1, count, "Table should exist");
        
        // Cleanup
        jdbcTemplate.execute("DROP TABLE IF EXISTS external_test_table");
    }

    @Test
    void canExecuteCRUDOperations() {
        // Setup
        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS crud_external_test (id SERIAL PRIMARY KEY, value VARCHAR(50))"
        );
        
        try {
            // CREATE
            jdbcTemplate.update("INSERT INTO crud_external_test (value) VALUES (?)", "test-value");
            
            // READ
            String value = jdbcTemplate.queryForObject(
                "SELECT value FROM crud_external_test WHERE value = ?",
                String.class,
                "test-value"
            );
            assertEquals("test-value", value);
            
            // UPDATE
            jdbcTemplate.update("UPDATE crud_external_test SET value = ? WHERE value = ?", 
                "updated-value", "test-value");
            String updated = jdbcTemplate.queryForObject(
                "SELECT value FROM crud_external_test WHERE value = ?",
                String.class,
                "updated-value"
            );
            assertEquals("updated-value", updated);
            
            // DELETE
            jdbcTemplate.update("DELETE FROM crud_external_test WHERE value = ?", "updated-value");
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM crud_external_test WHERE value = ?",
                Integer.class,
                "updated-value"
            );
            assertEquals(0, count, "Record should be deleted");
        } finally {
            // Cleanup
            jdbcTemplate.execute("DROP TABLE IF EXISTS crud_external_test");
        }
    }

    @Test
    void transactionsWork() {
        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS tx_external_test (id SERIAL PRIMARY KEY, value VARCHAR(50))"
        );
        
        try {
            jdbcTemplate.update("INSERT INTO tx_external_test (value) VALUES (?)", "tx-test");
            
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tx_external_test",
                Integer.class
            );
            assertTrue(count >= 1, "Should have at least one record");
        } finally {
            jdbcTemplate.execute("DROP TABLE IF EXISTS tx_external_test");
        }
    }
}
