package com.imipscanning.sentinelreports.common.service;

import com.imipscanning.sentinelreports.common.api.SentinelReportsAPI;
import com.imipscanning.sentinelreports.common.model.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ReportService implements SentinelReportsAPI {
    private final ReportCreationService creationService;
    private final ReportAssignmentService assignmentService;
    private final ReportValidationService validationService;
    private final ReportSearchService searchService;
    private final ReportHistoryService historyService;
    private final ReportStatsService statsService;
    private final ReportExportService exportService;
    private final com.imipscanning.sentinelreports.common.storage.ReportRepository reportRepository;
    private final com.imipscanning.sentinelreports.common.storage.EvidenceRepository evidenceRepository;
    private final com.imipscanning.sentinelreports.common.storage.NoteRepository noteRepository;
    private final com.imipscanning.sentinelreports.common.storage.CategoryRepository categoryRepository;
    private final com.imipscanning.sentinelreports.common.storage.ActionRepository actionRepository;
    private final DiscordWebhookService discordWebhookService;

    public ReportService(
            ReportCreationService creationService,
            ReportAssignmentService assignmentService,
            ReportValidationService validationService,
            ReportSearchService searchService,
            ReportHistoryService historyService,
            ReportStatsService statsService,
            ReportExportService exportService,
            com.imipscanning.sentinelreports.common.storage.ReportRepository reportRepository,
            com.imipscanning.sentinelreports.common.storage.EvidenceRepository evidenceRepository,
            com.imipscanning.sentinelreports.common.storage.NoteRepository noteRepository,
            com.imipscanning.sentinelreports.common.storage.CategoryRepository categoryRepository,
            com.imipscanning.sentinelreports.common.storage.ActionRepository actionRepository,
            DiscordWebhookService discordWebhookService
    ) {
        this.creationService = creationService;
        this.assignmentService = assignmentService;
        this.validationService = validationService;
        this.searchService = searchService;
        this.historyService = historyService;
        this.statsService = statsService;
        this.exportService = exportService;
        this.reportRepository = reportRepository;
        this.evidenceRepository = evidenceRepository;
        this.noteRepository = noteRepository;
        this.categoryRepository = categoryRepository;
        this.actionRepository = actionRepository;
        this.discordWebhookService = discordWebhookService;
    }

    @Override
    public Report createReport(ReportCreateRequest request) {
        return creationService.create(request);
    }

    @Override
    public Report closeReport(long reportId, ReportStatus status, UUID actorUuid, String actorName, String reason) {
        validationService.requireCloseReason(reason);
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ValidationException("errors.invalid_report", "Report not found"));
        if (!report.status().canTransitionTo(status)) {
            throw new ValidationException("errors.invalid_status", "Invalid status transition");
        }
        if (!status.isTerminal()) {
            throw new ValidationException("errors.invalid_status", "Close status must be terminal");
        }
        Report closed = report.closed(status, actorUuid, actorName, reason, System.currentTimeMillis());
        reportRepository.updateStatus(closed);
        actionRepository.add(reportId, "CLOSE_" + status.name(), actorUuid, actorName, report.status().name(), status.name());
        discordWebhookService.sendClosed(closed);
        return closed;
    }

    public Report updateStatus(long reportId, ReportStatus status, UUID actorUuid, String actorName) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ValidationException("errors.invalid_report", "Report not found"));
        if (!report.status().canTransitionTo(status)) {
            throw new ValidationException("errors.invalid_status", "Invalid status transition");
        }
        Report updated = status.isTerminal()
                ? report.closed(status, actorUuid, actorName, "Status changed to " + status.name(), System.currentTimeMillis())
                : report.withStatus(status, System.currentTimeMillis());
        reportRepository.updateStatus(updated);
        actionRepository.add(reportId, "STATUS_CHANGE", actorUuid, actorName, report.status().name(), status.name());
        return updated;
    }

    public Report updatePriority(long reportId, ReportPriority priority, UUID actorUuid, String actorName) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ValidationException("errors.invalid_report", "Report not found"));
        reportRepository.updatePriority(reportId, priority, System.currentTimeMillis());
        actionRepository.add(reportId, "PRIORITY_CHANGE", actorUuid, actorName, report.priority().name(), priority.name());
        return report.withPriority(priority, System.currentTimeMillis());
    }

    @Override
    public ReportEvidence addEvidence(long reportId, String content, UUID actorUuid, String actorName) {
        if (!com.imipscanning.sentinelreports.common.util.StringSanitizer.safeEvidence(content)) {
            throw new ValidationException("errors.invalid_link", "Invalid evidence");
        }
        reportRepository.findById(reportId).orElseThrow(() -> new ValidationException("errors.invalid_report", "Report not found"));
        ReportEvidence evidence = evidenceRepository.add(new ReportEvidence(0L, reportId,
                EvidenceType.detect(content), com.imipscanning.sentinelreports.common.util.StringSanitizer.clean(content),
                actorUuid, actorName, System.currentTimeMillis()));
        actionRepository.add(reportId, "EVIDENCE_ADD", actorUuid, actorName, null, evidence.content());
        return evidence;
    }

    @Override
    public ReportNote addNote(long reportId, String note, UUID actorUuid, String actorName) {
        reportRepository.findById(reportId).orElseThrow(() -> new ValidationException("errors.invalid_report", "Report not found"));
        ReportNote saved = noteRepository.add(new ReportNote(0L, reportId,
                com.imipscanning.sentinelreports.common.util.StringSanitizer.note(note),
                actorUuid, actorName, System.currentTimeMillis()));
        actionRepository.add(reportId, "NOTE_ADD", actorUuid, actorName, null, saved.note());
        return saved;
    }

    public boolean removeEvidence(long reportId, long evidenceId, UUID actorUuid, String actorName) {
        boolean removed = evidenceRepository.remove(reportId, evidenceId);
        if (removed) {
            actionRepository.add(reportId, "EVIDENCE_REMOVE", actorUuid, actorName, String.valueOf(evidenceId), null);
        }
        return removed;
    }

    public Report claim(long reportId, UUID staffUuid, String staffName) {
        return assignmentService.claim(reportId, staffUuid, staffName);
    }

    public Report unclaim(long reportId, UUID actorUuid, String actorName) {
        return assignmentService.unclaim(reportId, actorUuid, actorName);
    }

    public Report assign(long reportId, UUID staffUuid, String staffName, UUID actorUuid, String actorName) {
        return assignmentService.assign(reportId, staffUuid, staffName, actorUuid, actorName);
    }

    @Override
    public Optional<Report> getReportById(long id) {
        return searchService.find(id);
    }

    @Override
    public List<Report> getReportsByPlayer(UUID playerUuid) {
        return historyService.reportsBy(playerUuid, 50);
    }

    public List<Report> getReportsAgainst(UUID playerUuid) {
        return historyService.reportsAgainst(playerUuid, 50);
    }

    @Override
    public List<Report> getOpenReports() {
        return searchService.firstPage(new ReportFilter(ReportStatus.OPEN, null, null, null, null, null, null, 0L, 0L)).items();
    }

    public com.imipscanning.sentinelreports.common.util.Pagination<Report> query(ReportQuery query) {
        return searchService.query(query);
    }

    public List<ReportEvidence> evidence(long reportId) {
        return evidenceRepository.list(reportId);
    }

    public List<ReportNote> notes(long reportId) {
        return noteRepository.list(reportId);
    }

    public List<ReportAction> actions(long reportId) {
        return actionRepository.list(reportId);
    }

    @Override
    public void registerCategory(ReportCategory category) {
        categoryRepository.upsert(category);
        categoryRepository.reload();
    }

    public List<ReportCategory> categories() {
        return categoryRepository.enabled();
    }

    @Override
    public PlayerReportStats getPlayerReportStats(UUID playerUuid) {
        return statsService.player(playerUuid, "Unknown");
    }

    public ReportStatsService stats() {
        return statsService;
    }

    public Path export(long reportId, String format) {
        return exportService.export(reportId, format);
    }

    public DiscordWebhookService discord() {
        return discordWebhookService;
    }
}
