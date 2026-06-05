package com.imipscanning.sentinelreports.common.service;

import com.imipscanning.sentinelreports.common.model.ReportPriority;
import com.imipscanning.sentinelreports.common.model.ReportStatus;

import java.nio.file.Path;

public record ReportSettings(
        boolean requireReason,
        int reasonTimeoutSeconds,
        ReportStatus defaultStatus,
        ReportPriority defaultPriority,
        boolean closeRequiresReason,
        Path exportFolder,
        String serverName
) {
}
