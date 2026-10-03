package com.backend.backend.security;

import java.time.Duration;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

@Service
public class RateLimitService {
    // * evicts IPs that haven't made a request in 15 minutes, caps the map at 10,000 active IPs to strictly bound memory usage
    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(15))
            .maximumSize(10000) 
            .build();

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(100)
                .refillGreedy(100, Duration.ofMinutes(15))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    public boolean tryConsume(String ipAddress) {
        Bucket bucket = cache.get(ipAddress, key -> createNewBucket());
        return bucket.tryConsume(1);
    }
}