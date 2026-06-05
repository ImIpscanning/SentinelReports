package com.imipscanning.sentinelreports.common.model;

import java.util.UUID;

public record ReportEvidence(
        long id,
        long reportId,
        EvidenceType evidenceType,
        String content,
        UUID addedByUuid,
        String addedByName,
        long createdAt
) {
}
