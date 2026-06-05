package com.imipscanning.sentinelreports.common.model;

import java.util.UUID;

public record PlayerReportStats(
        UUID playerUuid,
        String playerName,
        long reportsCreated,
        long reportsAgainst,
        long openReports,
        long falseReports,
        long resolvedReports
) {
}
