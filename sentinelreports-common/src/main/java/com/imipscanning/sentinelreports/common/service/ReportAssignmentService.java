package com.imipscanning.sentinelreports.common.service;

import com.imipscanning.sentinelreports.common.model.Report;
import com.imipscanning.sentinelreports.common.model.ReportStatus;
import com.imipscanning.sentinelreports.common.storage.ActionRepository;
import com.imipscanning.sentinelreports.common.storage.ReportRepository;

import java.util.UUID;

public final class ReportAssignmentService {
    private final ReportRepository reportRepository;
    private final ActionRepository actionRepository;
    private final DiscordWebhookService discordWebhookService;

    public ReportAssignmentService(ReportRepository reportRepository, ActionRepository actionRepository, DiscordWebhookService discordWebhookService) {
        this.reportRepository = reportRepository;
        this.actionRepository = actionRepository;
        this.discordWebhookService = discordWebhookService;
    }

    public Report claim(long reportId, UUID staffUuid, String staffName) {
        return assign(reportId, staffUuid, staffName, staffUuid, staffName);
    }

    public Report assign(long reportId, UUID staffUuid, String staffName, UUID actorUuid, String actorName) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ValidationException("errors.invalid_report", "Report not found"));
        if (report.status().isTerminal()) {
            throw new ValidationException("errors.invalid_status", "Report is already closed");
        }
        long now = System.currentTimeMillis();
        reportRepository.assign(reportId, staffUuid, staffName, now);
        Report updated = report.withAssignment(staffUuid, staffName, now);
        actionRepository.add(reportId, staffUuid == null ? "UNCLAIM" : "CLAIM", actorUuid, actorName,
                report.assignedToName(), staffName);
        if (staffUuid != null && updated.status() == ReportStatus.ASSIGNED) {
            discordWebhookService.sendAssigned(updated);
        }
        return updated;
    }

    public Report unclaim(long reportId, UUID actorUuid, String actorName) {
        return assign(reportId, null, null, actorUuid, actorName);
    }
}
