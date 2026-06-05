package com.imipscanning.sentinelreports.common.api;

import com.imipscanning.sentinelreports.common.model.Report;

import java.util.UUID;

public record ReportAssignEvent(Report report, UUID staffUuid, String staffName) {
}
