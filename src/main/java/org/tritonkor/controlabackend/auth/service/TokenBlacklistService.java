package org.tritonkor.controlabackend.auth.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final Map<String, Instant> blacklist = new ConcurrentHashMap<>();

    public void blacklist(String token, Instant expiresAt) {
        blacklist.put(token, expiresAt);
    }

    public boolean isBlacklisted(String token) {
        Instant exp = blacklist.get(token);
        if (exp == null) return false;
        if (Instant.now().isAfter(exp)) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }

    // 10 minutes
    @Scheduled(fixedDelay = 600_000)
    public void evictExpiredTokens() {
        Instant now = Instant.now();
        blacklist.entrySet().removeIf(entry -> now.isAfter(entry.getValue()));
    }
}

