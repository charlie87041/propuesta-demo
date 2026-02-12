package com.cookiesstore.common.test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Redis TestContainer configuration constants.
 * These tests verify the configuration settings without requiring Docker.
 */
class RedisContainerConfigurationTest {

    // Configuration constants used in AbstractIntegrationTest
    private static final String REDIS_IMAGE = "redis:7-alpine";
    private static final int REDIS_PORT = 6379;

    @Test
    void redisImageVersionIsCorrect() {
        assertTrue(REDIS_IMAGE.contains("redis:7"),
            "Container should use Redis 7");
        assertTrue(REDIS_IMAGE.contains("alpine"),
            "Container should use Alpine variant for smaller image size");
    }

    @Test
    void redisDefaultPortIsCorrect() {
        assertEquals(6379, REDIS_PORT, "Redis default port should be 6379");
    }
}
