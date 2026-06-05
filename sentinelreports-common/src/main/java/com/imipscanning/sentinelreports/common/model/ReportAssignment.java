package com.imipscanning.sentinelreports.common.model;

import java.util.UUID;

public record ReportAssignment(
        long reportId,
        UUID assignedToUuid,
        String assignedToName,
        long assignedAt
) {
    public boolean assigned() {
        return assignedToUuid != null;
    }
}
