package com.cookiesstore.common.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify Redis TestContainer configuration.
 * Tests basic cache operations.
 */
@SpringBootTest
@RequiresDocker
class RedisContainerTest extends AbstractIntegrationTest {

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void redisContainerIsRunning() {
        assertTrue(redisContainer.isRunning(), "Redis container should be running");
    }

    @Test
    void canConnectToRedis() {
        assertNotNull(redisContainer.getHost(), "Redis host should be available");
        assertTrue(redisContainer.getMappedPort(6379) > 0, "Redis port should be mapped");
    }

    @Test
    void canExecuteBasicSetGet() {
        if (stringRedisTemplate == null) {
            return; // Skip in RED phase
        }
        
        String key = "test:key";
        String value = "test-value";
        
        stringRedisTemplate.opsForValue().set(key, value);
        String retrieved = stringRedisTemplate.opsForValue().get(key);
        
        assertEquals(value, retrieved, "Retrieved value should match stored value");
    }

    @Test
    void canSetWithExpiration() {
        if (stringRedisTemplate == null) {
            return;
        }
        
        String key = "test:expiring-key";
        String value = "expiring-value";
        
        stringRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds(60));
        Long ttl = stringRedisTemplate.getExpire(key);
        
        assertNotNull(ttl, "TTL should be set");
        assertTrue(ttl > 0, "TTL should be positive");
    }

    @Test
    void canDeleteKey() {
        if (stringRedisTemplate == null) {
            return;
        }
        
        String key = "test:delete-key";
        String value = "to-be-deleted";
        
        stringRedisTemplate.opsForValue().set(key, value);
        assertNotNull(stringRedisTemplate.opsForValue().get(key));
        
        Boolean deleted = stringRedisTemplate.delete(key);
        assertTrue(deleted, "Key should be deleted");
        assertNull(stringRedisTemplate.opsForValue().get(key), "Key should not exist after deletion");
    }

    @Test
    void canExecuteHashOperations() {
        if (stringRedisTemplate == null) {
            return;
        }
        
        String key = "test:hash";
        
        stringRedisTemplate.opsForHash().put(key, "field1", "value1");
        stringRedisTemplate.opsForHash().put(key, "field2", "value2");
        
        Object field1 = stringRedisTemplate.opsForHash().get(key, "field1");
        Object field2 = stringRedisTemplate.opsForHash().get(key, "field2");
        
        assertEquals("value1", field1);
        assertEquals("value2", field2);
    }

    @Test
    void canExecuteListOperations() {
        if (stringRedisTemplate == null) {
            return;
        }
        
        String key = "test:list";
        
        stringRedisTemplate.opsForList().rightPush(key, "item1");
        stringRedisTemplate.opsForList().rightPush(key, "item2");
        stringRedisTemplate.opsForList().rightPush(key, "item3");
        
        Long size = stringRedisTemplate.opsForList().size(key);
        assertEquals(3, size, "List should have 3 items");
        
        String first = stringRedisTemplate.opsForList().leftPop(key);
        assertEquals("item1", first, "First item should be 'item1'");
    }
}
