package com.imipscanning.sentinelreports.common.model;

import java.util.UUID;

public record StaffReportStats(
        UUID staffUuid,
        String staffName,
        long claimed,
        long resolved,
        long rejected,
        long falseReports,
        long notesAdded,
        long evidenceAdded
) {
}
