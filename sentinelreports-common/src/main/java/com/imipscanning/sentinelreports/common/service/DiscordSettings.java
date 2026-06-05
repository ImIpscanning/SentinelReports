package com.imipscanning.sentinelreports.common.service;

public record DiscordSettings(
        boolean enabled,
        String webhookUrl,
        String username,
        String avatarUrl,
        boolean notifyNewReport,
        boolean notifyCriticalReport,
        boolean notifyReportClosed,
        boolean notifyReportAssigned,
        boolean notifyFalseReport,
        long cooldownMillis
) {
}
