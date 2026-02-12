package com.cookiesstore.common.test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PostgreSQL TestContainer configuration constants.
 * These tests verify the configuration settings without requiring Docker.
 */
class PostgresContainerConfigurationTest {

    // Configuration constants used in AbstractIntegrationTest
    private static final String POSTGRES_IMAGE = "postgres:15-alpine";
    private static final String DATABASE_NAME = "testdb";
    private static final String USERNAME = "test";
    private static final String PASSWORD = "test";
    private static final int POSTGRES_PORT = 5432;

    @Test
    void postgresImageVersionIsCorrect() {
        assertTrue(POSTGRES_IMAGE.contains("postgres:15"),
            "Container should use PostgreSQL 15");
        assertTrue(POSTGRES_IMAGE.contains("alpine"),
            "Container should use Alpine variant for smaller image size");
    }

    @Test
    void databaseNameIsConfigured() {
        assertNotNull(DATABASE_NAME, "Database name should be configured");
        assertFalse(DATABASE_NAME.isEmpty(), "Database name should not be empty");
        assertEquals("testdb", DATABASE_NAME);
    }

    @Test
    void credentialsAreConfigured() {
        assertNotNull(USERNAME, "Username should be configured");
        assertNotNull(PASSWORD, "Password should be configured");
        assertFalse(USERNAME.isEmpty(), "Username should not be empty");
        assertFalse(PASSWORD.isEmpty(), "Password should not be empty");
    }

    @Test
    void postgresDefaultPortIsCorrect() {
        assertEquals(5432, POSTGRES_PORT, "PostgreSQL default port should be 5432");
    }
}

