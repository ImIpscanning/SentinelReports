package com.imipscanning.sentinelreports.common.api;

import com.imipscanning.sentinelreports.common.model.Report;
import com.imipscanning.sentinelreports.common.model.ReportStatus;

public record ReportStatusChangeEvent(Report report, ReportStatus oldStatus, ReportStatus newStatus) {
}
