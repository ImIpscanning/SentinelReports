package com.imipscanning.sentinelreports.common.model;

public record ReportCategory(
        String id,
        String name,
        String material,
        ReportPriority defaultPriority,
        String permission,
        String description,
        long cooldownMillis,
        String discordMessage,
        boolean enabled
) {
    public ReportCategory {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Category id cannot be blank");
        }
        id = id.trim().toLowerCase();
        name = name == null || name.isBlank() ? id : name;
        material = material == null || material.isBlank() ? "PAPER" : material;
        defaultPriority = defaultPriority == null ? ReportPriority.MEDIUM : defaultPriority;
        permission = permission == null || permission.isBlank() ? null : permission;
        description = description == null ? "" : description;
        discordMessage = discordMessage == null || discordMessage.isBlank() ? null : discordMessage;
    }
}
