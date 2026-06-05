package com.imipscanning.sentinelreports.common.model;

import java.util.UUID;

public record ReportFilter(
        ReportStatus status,
        ReportPriority priority,
        String category,
        String serverName,
        UUID reporterUuid,
        UUID targetUuid,
        UUID assignedToUuid,
        long createdAfter,
        long createdBefore
) {
    public static ReportFilter empty() {
        return new ReportFilter(null, null, null, null, null, null, null, 0L, 0L);
    }
}
