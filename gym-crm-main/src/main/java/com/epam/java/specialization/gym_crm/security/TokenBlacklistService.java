package com.epam.java.specialization.gym_crm.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private final RedisTemplate<String, Object> redisTemplate;
    private final long jwtExpirationMs;
    private final Set<String> localBlacklist = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public TokenBlacklistService(
            @Autowired(required = false) RedisTemplate<String, Object> redisTemplate,
            @Value("${application.security.jwt.expiration}") long jwtExpirationMs) {
        this.redisTemplate = redisTemplate;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public void blacklistToken(String token) {
        if (token != null && !token.isBlank()) {
            String key = BLACKLIST_PREFIX + token;
            if (redisTemplate != null) {
                redisTemplate.opsForValue().set(key, true, jwtExpirationMs, TimeUnit.MILLISECONDS);
            } else {
                localBlacklist.add(key);
            }
        }
    }

    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String key = BLACKLIST_PREFIX + token;
        if (redisTemplate != null) {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        }
        return localBlacklist.contains(key);
    }
}