package com.imipscanning.sentinelreports.common.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CooldownService {
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();

    public void mark(String scope, UUID playerUuid, long cooldownMillis) {
        if (cooldownMillis <= 0) {
            return;
        }
        cooldowns.put(key(scope, playerUuid), System.currentTimeMillis() + cooldownMillis);
    }

    public long remainingMillis(String scope, UUID playerUuid) {
        Long expiresAt = cooldowns.get(key(scope, playerUuid));
        if (expiresAt == null) {
            return 0L;
        }
        long remaining = expiresAt - System.currentTimeMillis();
        if (remaining <= 0) {
            cooldowns.remove(key(scope, playerUuid));
            return 0L;
        }
        return remaining;
    }

    public boolean active(String scope, UUID playerUuid) {
        return remainingMillis(scope, playerUuid) > 0;
    }

    public void clear(UUID playerUuid) {
        cooldowns.keySet().removeIf(key -> key.endsWith(":" + playerUuid));
    }

    private String key(String scope, UUID playerUuid) {
        return scope + ":" + playerUuid;
    }
}
