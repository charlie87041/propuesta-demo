package com.cookiesstore.common.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private JwtTokenProvider buildProvider(Duration expiration) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("this-is-a-test-secret-key-with-at-least-32-bytes-long-1234567890");
        properties.setExpiration(expiration);
        return new JwtTokenProvider(properties);
    }

    @Test
    void generatesTokenForValidUserId() {
        JwtTokenProvider jwtTokenProvider = buildProvider(Duration.ofHours(1));
        String token = jwtTokenProvider.generateToken(123L);

        assertNotNull(token);
        assertTrue(token.length() > 20);
    }

    @Test
    void extractsUserIdFromToken() {
        JwtTokenProvider jwtTokenProvider = buildProvider(Duration.ofHours(1));
        String token = jwtTokenProvider.generateToken(456L);

        Long userId = jwtTokenProvider.extractUserId(token);

        assertEquals(456L, userId);
    }

    @Test
    void returnsFalseForExpiredToken() throws InterruptedException {
        JwtTokenProvider shortLivedProvider = buildProvider(Duration.ofMillis(10));

        String token = shortLivedProvider.generateToken(789L);
        Thread.sleep(25);

        assertFalse(shortLivedProvider.isValid(token));
    }

    @Property
    void roundTripUserId(@ForAll long userId) {
        JwtTokenProvider jwtTokenProvider = buildProvider(Duration.ofHours(1));
        long normalizedUserId = Math.max(1L, userId);

        String token = jwtTokenProvider.generateToken(normalizedUserId);
        Long extracted = jwtTokenProvider.extractUserId(token);

        assertEquals(normalizedUserId, extracted);
    }
}
