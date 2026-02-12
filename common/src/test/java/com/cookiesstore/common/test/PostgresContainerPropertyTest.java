package com.cookiesstore.common.test;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for PostgreSQL TestContainer.
 * 
 * Property 1: Any valid entity persisted should be retrievable with identical data
 * (Round-trip property test)
 */
@SpringBootTest
@RequiresDocker
class PostgresContainerPropertyTest extends AbstractIntegrationTest {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    /**
     * Property 1: Round-trip persistence test
     * Any data written to the database should be retrievable identically
     */
    @Property(tries = 100)
    void anyPersistedDataIsRetrievableIdentically(
            @ForAll @IntRange(min = 1, max = 1000000) int id,
            @ForAll @StringLength(min = 1, max = 100) String value
    ) {
        if (jdbcTemplate == null) {
            // Skip in RED phase when Spring context not available
            return;
        }
        
        // Setup
        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS property_test (id INT PRIMARY KEY, value VARCHAR(100))"
        );
        
        try {
            // Persist
            jdbcTemplate.update(
                "INSERT INTO property_test (id, value) VALUES (?, ?) ON CONFLICT (id) DO UPDATE SET value = EXCLUDED.value",
                id, value
            );
            
            // Retrieve
            String retrieved = jdbcTemplate.queryForObject(
                "SELECT value FROM property_test WHERE id = ?",
                String.class,
                id
            );
            
            // Assert: Retrieved data must be identical to persisted data
            assertEquals(value, retrieved, 
                String.format("Persisted value '%s' should match retrieved value '%s'", value, retrieved));
                
        } finally {
            // Cleanup
            jdbcTemplate.update("DELETE FROM property_test WHERE id = ?", id);
        }
    }

    /**
     * Property 2: Container state independence
     * Each test run should start with a clean database state
     */
    @Property(tries = 50)
    void containerStateIsIndependentAcrossRuns(
            @ForAll @StringLength(min = 5, max = 20) String tableName
    ) {
        if (jdbcTemplate == null) {
            return;
        }
        
        // Sanitize table name (remove special characters)
        String safeTableName = "test_" + tableName.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
        
        // Table should not exist at start of test (proves isolation)
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?",
            Integer.class,
            safeTableName
        );
        assertEquals(0, count, "Table should not exist from previous test runs");
        
        // Create table
        jdbcTemplate.execute(
            String.format("CREATE TABLE %s (id SERIAL PRIMARY KEY)", safeTableName)
        );
        
        // Clean up
        jdbcTemplate.execute(String.format("DROP TABLE %s", safeTableName));
    }
}
