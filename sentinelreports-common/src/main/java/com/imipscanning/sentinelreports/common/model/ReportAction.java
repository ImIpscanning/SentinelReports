package com.imipscanning.sentinelreports.common.model;

import java.util.UUID;

public record ReportAction(
        long id,
        long reportId,
        String actionType,
        UUID actorUuid,
        String actorName,
        String oldValue,
        String newValue,
        long createdAt
) {
}
