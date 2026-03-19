package com.cookiesstore.common.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test using external Redis container.
 * Run with: ./gradlew test --tests "*ExternalRedisTest" -Dspring.profiles.active=external
 */
class ExternalRedisTest extends AbstractExternalContainerTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void canConnectToExternalRedis() {
        assertNotNull(stringRedisTemplate, "StringRedisTemplate should be autowired");
        
        // Test ping
        String pong = stringRedisTemplate.getConnectionFactory()
            .getConnection()
            .ping();
        assertEquals("PONG", pong, "Redis should respond to PING");
    }

    @Test
    void canExecuteBasicSetGet() {
        String key = "external-test:key:" + System.currentTimeMillis();
        String value = "test-value";
        
        try {
            stringRedisTemplate.opsForValue().set(key, value);
            String retrieved = stringRedisTemplate.opsForValue().get(key);
            
            assertEquals(value, retrieved, "Retrieved value should match stored value");
        } finally {
            stringRedisTemplate.delete(key);
        }
    }

    @Test
    void canSetWithExpiration() {
        String key = "external-test:expiring:" + System.currentTimeMillis();
        String value = "expiring-value";
        
        try {
            stringRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds(60));
            Long ttl = stringRedisTemplate.getExpire(key);
            
            assertNotNull(ttl, "TTL should be set");
            assertTrue(ttl > 0 && ttl <= 60, "TTL should be positive and <= 60");
        } finally {
            stringRedisTemplate.delete(key);
        }
    }

    @Test
    void canDeleteKey() {
        String key = "external-test:delete:" + System.currentTimeMillis();
        String value = "to-be-deleted";
        
        stringRedisTemplate.opsForValue().set(key, value);
        assertNotNull(stringRedisTemplate.opsForValue().get(key));
        
        Boolean deleted = stringRedisTemplate.delete(key);
        assertTrue(deleted, "Key should be deleted");
        assertNull(stringRedisTemplate.opsForValue().get(key), "Key should not exist after deletion");
    }

    @Test
    void canExecuteHashOperations() {
        String key = "external-test:hash:" + System.currentTimeMillis();
        
        try {
            stringRedisTemplate.opsForHash().put(key, "field1", "value1");
            stringRedisTemplate.opsForHash().put(key, "field2", "value2");
            
            Object field1 = stringRedisTemplate.opsForHash().get(key, "field1");
            Object field2 = stringRedisTemplate.opsForHash().get(key, "field2");
            
            assertEquals("value1", field1);
            assertEquals("value2", field2);
        } finally {
            stringRedisTemplate.delete(key);
        }
    }

    @Test
    void canExecuteListOperations() {
        String key = "external-test:list:" + System.currentTimeMillis();
        
        try {
            stringRedisTemplate.opsForList().rightPush(key, "item1");
            stringRedisTemplate.opsForList().rightPush(key, "item2");
            stringRedisTemplate.opsForList().rightPush(key, "item3");
            
            Long size = stringRedisTemplate.opsForList().size(key);
            assertEquals(3, size, "List should have 3 items");
            
            String first = stringRedisTemplate.opsForList().leftPop(key);
            assertEquals("item1", first, "First item should be 'item1'");
        } finally {
            stringRedisTemplate.delete(key);
        }
    }

    @Test
    void canExecuteSetOperations() {
        String key = "external-test:set:" + System.currentTimeMillis();
        
        try {
            stringRedisTemplate.opsForSet().add(key, "member1", "member2", "member3");
            
            Long size = stringRedisTemplate.opsForSet().size(key);
            assertEquals(3, size, "Set should have 3 members");
            
            Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, "member1");
            assertTrue(isMember, "member1 should be in set");
        } finally {
            stringRedisTemplate.delete(key);
        }
    }
}
