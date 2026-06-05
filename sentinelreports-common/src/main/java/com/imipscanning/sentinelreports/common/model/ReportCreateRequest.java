package com.imipscanning.sentinelreports.common.model;

import java.util.UUID;

public record ReportCreateRequest(
        UUID reporterUuid,
        String reporterName,
        UUID targetUuid,
        String targetName,
        String category,
        String reason,
        String serverName,
        String world,
        Double x,
        Double y,
        Double z,
        boolean targetIsStaff,
        boolean bypassCooldown,
        boolean bypassLimit
) {
}
