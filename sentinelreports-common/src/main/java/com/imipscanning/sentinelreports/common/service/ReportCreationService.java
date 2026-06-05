package com.imipscanning.sentinelreports.common.service;

import com.imipscanning.sentinelreports.common.model.Report;
import com.imipscanning.sentinelreports.common.model.ReportCategory;
import com.imipscanning.sentinelreports.common.model.ReportCreateRequest;
import com.imipscanning.sentinelreports.common.storage.ActionRepository;
import com.imipscanning.sentinelreports.common.storage.ReportRepository;
import com.imipscanning.sentinelreports.common.util.StringSanitizer;

import java.util.UUID;

public final class ReportCreationService {
    private final ReportSettings reportSettings;
    private final ReportValidationService validationService;
    private final AbuseDetectionService abuseDetectionService;
    private final ReportRepository reportRepository;
    private final ActionRepository actionRepository;
    private final ReportNotificationService notificationService;
    private final DiscordWebhookService discordWebhookService;

    public ReportCreationService(
            ReportSettings reportSettings,
            ReportValidationService validationService,
            AbuseDetectionService abuseDetectionService,
            ReportRepository reportRepository,
            ActionRepository actionRepository,
            ReportNotificationService notificationService,
            DiscordWebhookService discordWebhookService
    ) {
        this.reportSettings = reportSettings;
        this.validationService = validationService;
        this.abuseDetectionService = abuseDetectionService;
        this.reportRepository = reportRepository;
        this.actionRepository = actionRepository;
        this.notificationService = notificationService;
        this.discordWebhookService = discordWebhookService;
    }

    public Report create(ReportCreateRequest request) {
        ReportCategory category = validationService.validateCreate(request);
        long now = System.currentTimeMillis();
        Report report = new Report(
                0L,
                UUID.randomUUID(),
                request.reporterUuid(),
                request.reporterName(),
                request.targetUuid(),
                request.targetName(),
                category.id(),
                StringSanitizer.reason(request.reason()),
                reportSettings.defaultStatus(),
                category.defaultPriority(),
                request.serverName() == null || request.serverName().isBlank() ? reportSettings.serverName() : request.serverName(),
                request.world(),
                request.x(),
                request.y(),
                request.z(),
                null,
                null,
                0L,
                now,
                now,
                null,
                null,
                null,
                null
        );
        Report saved = reportRepository.create(report);
        actionRepository.add(saved.id(), "CREATE", request.reporterUuid(), request.reporterName(), null, saved.status().name());
        abuseDetectionService.markCreated(request, category);
        notificationService.notifyNewReport(saved);
        discordWebhookService.sendNewReport(saved);
        return saved;
    }
}
