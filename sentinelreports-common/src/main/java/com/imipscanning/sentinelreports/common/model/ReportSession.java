package com.imipscanning.sentinelreports.common.model;

import java.util.UUID;

public record ReportSession(
        UUID reporterUuid,
        UUID targetUuid,
        String targetName,
        String category,
        long expiresAt
) {
    public boolean expired(long now) {
        return now >= expiresAt;
    }
}
