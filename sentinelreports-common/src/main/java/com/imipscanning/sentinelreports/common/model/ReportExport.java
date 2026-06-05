package com.imipscanning.sentinelreports.common.model;

import java.util.List;

public record ReportExport(
        Report report,
        List<ReportEvidence> evidence,
        List<ReportNote> notes,
        List<ReportAction> actions,
        long exportedAt
) {
}
