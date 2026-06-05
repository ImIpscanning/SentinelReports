package com.imipscanning.sentinelreports.common.service;

public record AbuseProtectionSettings(
        boolean enabled,
        long globalCooldownMillis,
        long sameTargetCooldownMillis,
        int maxOpenReportsPerPlayer,
        boolean blockSelfReports,
        boolean blockDuplicateReports,
        long duplicateWindowMillis,
        int falseReportThreshold,
        boolean allowReportingStaff
) {
}
