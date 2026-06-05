package com.imipscanning.sentinelreports.common.model;

import java.util.UUID;

public record Report(
        long id,
        UUID reportUuid,
        UUID reporterUuid,
        String reporterName,
        UUID targetUuid,
        String targetName,
        String category,
        String reason,
        ReportStatus status,
        ReportPriority priority,
        String serverName,
        String world,
        Double x,
        Double y,
        Double z,
        UUID assignedToUuid,
        String assignedToName,
        long assignedAt,
        long createdAt,
        long updatedAt,
        Long closedAt,
        UUID closedByUuid,
        String closedByName,
        String closeReason
) {
    public Report {
        if (reportUuid == null) {
            reportUuid = UUID.randomUUID();
        }
        if (reporterUuid == null || targetUuid == null) {
            throw new IllegalArgumentException("Reporter and target UUIDs are required");
        }
        reporterName = reporterName == null ? "Unknown" : reporterName;
        targetName = targetName == null ? "Unknown" : targetName;
        category = category == null ? "other" : category.toLowerCase();
        reason = reason == null ? "" : reason;
        status = status == null ? ReportStatus.OPEN : status;
        priority = priority == null ? ReportPriority.MEDIUM : priority;
        serverName = serverName == null || serverName.isBlank() ? "unknown" : serverName;
    }

    public Report withId(long id) {
        return new Report(id, reportUuid, reporterUuid, reporterName, targetUuid, targetName, category, reason, status,
                priority, serverName, world, x, y, z, assignedToUuid, assignedToName, assignedAt, createdAt, updatedAt,
                closedAt, closedByUuid, closedByName, closeReason);
    }

    public Report withStatus(ReportStatus newStatus, long now) {
        return new Report(id, reportUuid, reporterUuid, reporterName, targetUuid, targetName, category, reason, newStatus,
                priority, serverName, world, x, y, z, assignedToUuid, assignedToName, assignedAt, createdAt, now,
                closedAt, closedByUuid, closedByName, closeReason);
    }

    public Report withPriority(ReportPriority newPriority, long now) {
        return new Report(id, reportUuid, reporterUuid, reporterName, targetUuid, targetName, category, reason, status,
                newPriority, serverName, world, x, y, z, assignedToUuid, assignedToName, assignedAt, createdAt, now,
                closedAt, closedByUuid, closedByName, closeReason);
    }

    public Report withAssignment(UUID staffUuid, String staffName, long now) {
        return new Report(id, reportUuid, reporterUuid, reporterName, targetUuid, targetName, category, reason,
                staffUuid == null ? ReportStatus.OPEN : ReportStatus.ASSIGNED, priority, serverName, world, x, y, z,
                staffUuid, staffName, staffUuid == null ? 0L : now, createdAt, now, closedAt, closedByUuid,
                closedByName, closeReason);
    }

    public Report closed(ReportStatus closeStatus, UUID actorUuid, String actorName, String reason, long now) {
        return new Report(id, reportUuid, reporterUuid, reporterName, targetUuid, targetName, category, this.reason,
                closeStatus, priority, serverName, world, x, y, z, assignedToUuid, assignedToName, assignedAt, createdAt,
                now, now, actorUuid, actorName, reason);
    }
}
