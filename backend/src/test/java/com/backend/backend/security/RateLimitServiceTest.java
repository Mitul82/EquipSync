package com.backend.backend.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RateLimitServiceTest {
    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService();
    }

    @Test
    public void tryConsume_WhenUnderLimit_ReturnsTrue() {
        boolean isAllowed = rateLimitService.tryConsume("192.168.1.100");

        assertTrue(isAllowed, "First request should always be allowed");
    }

    @Test
    public void tryConsume_WhenLimitExhausted_ReturnsFalse() {
        String testIp = "192.168.1.101";

        for (int i = 0; i < 100; i++) {
            boolean isAllowed = rateLimitService.tryConsume(testIp);
            assertTrue(isAllowed, "Request " + (i + 1) + " should be allowed");
        }

        boolean isBlocked = rateLimitService.tryConsume(testIp);

        assertFalse(isBlocked, "The 101st request should be blocked by Bucket4j");
    }

    @Test
    public void tryConsume_IsolatesDifferentIpAddresses() {
        String spammerIp = "192.168.1.200";
        String innocentIp = "192.168.1.201";

        for (int i = 0; i < 100; i++) {
            rateLimitService.tryConsume(spammerIp);
        }

        assertFalse(rateLimitService.tryConsume(spammerIp), "Spammer should be blocked");

        boolean innocentAllowed = rateLimitService.tryConsume(innocentIp);
        assertTrue(innocentAllowed, "A different IP should have its own fresh bucket of 100 tokens");
    }
}