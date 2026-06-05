package com.imipscanning.sentinelreports.common.model;

public record ReportHubButton(
        String id,
        String name,
        String description,
        String command,
        String permission,
        boolean suggestCommand
) {
}
