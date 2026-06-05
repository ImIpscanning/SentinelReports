package com.imipscanning.sentinelreports.common.model;

import java.util.List;

public record ReportHubCategory(
        String id,
        String name,
        List<ReportHubButton> buttons
) {
}
