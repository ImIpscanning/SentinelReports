package com.imipscanning.sentinelreports.common.model;

import java.util.UUID;

public record ReportNote(
        long id,
        long reportId,
        String note,
        UUID addedByUuid,
        String addedByName,
        long createdAt
) {
}
