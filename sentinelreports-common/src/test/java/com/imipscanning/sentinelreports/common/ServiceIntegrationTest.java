package com.imipscanning.sentinelreports.common;

import com.imipscanning.sentinelreports.common.model.*;
import com.imipscanning.sentinelreports.common.service.*;
import com.imipscanning.sentinelreports.common.storage.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceIntegrationTest {
    @TempDir
    Path tempDir;

    @Test
    void validatesCreatesAndExportsReport() {
        try (Fixtures fixtures = fixtures()) {
            fixtures.categoryRepository.upsertAll(List.of(new ReportCategory("cheating", "Cheating", "DIAMOND_SWORD",
                    ReportPriority.HIGH, null, "Hacks", 0L, null, true)));
            UUID reporter = UUID.randomUUID();
            UUID target = UUID.randomUUID();
            Report report = fixtures.reportService.createReport(new ReportCreateRequest(reporter, "Reporter", target, "Target",
                    "cheating", "Reach sospechoso", "test", "world", 1.0, 64.0, 2.0, false, false, false));

            fixtures.reportService.addEvidence(report.id(), "https://example.com/video.mp4", reporter, "Reporter");
            fixtures.reportService.addNote(report.id(), "Revisar replay", reporter, "Reporter");
            Path export = fixtures.reportService.export(report.id(), "json");

            assertThat(report.id()).isPositive();
            assertThat(report.priority()).isEqualTo(ReportPriority.HIGH);
            assertThat(export).exists();
        }
    }

    @Test
    void blocksSelfReportsWhenConfigured() {
        try (Fixtures fixtures = fixtures()) {
            fixtures.categoryRepository.upsertAll(List.of(new ReportCategory("other", "Other", "PAPER",
                    ReportPriority.LOW, null, "", 0L, null, true)));
            UUID player = UUID.randomUUID();
            assertThatThrownBy(() -> fixtures.reportService.createReport(new ReportCreateRequest(player, "Player", player, "Player",
                    "other", "self", "test", null, null, null, null, false, false, false)))
                    .isInstanceOf(ValidationException.class);
        }
    }

    private Fixtures fixtures() {
        DatabaseManager databaseManager = new DatabaseManager(new DatabaseSettings(DatabaseType.SQLITE,
                tempDir.resolve(UUID.randomUUID() + ".db"), "", 0, "", "", "", 1));
        databaseManager.start();
        new MigrationManager(databaseManager).migrate();
        ReportRepository reportRepository = new ReportRepository(databaseManager);
        EvidenceRepository evidenceRepository = new EvidenceRepository(databaseManager);
        NoteRepository noteRepository = new NoteRepository(databaseManager);
        CategoryRepository categoryRepository = new CategoryRepository(databaseManager);
        ActionRepository actionRepository = new ActionRepository(databaseManager);
        ReportSettings reportSettings = new ReportSettings(true, 60, ReportStatus.OPEN, ReportPriority.MEDIUM,
                true, tempDir.resolve("exports"), "test");
        AbuseProtectionSettings abuse = new AbuseProtectionSettings(true, 0L, 0L, 5,
                true, true, 1_000L, 5, true);
        AbuseDetectionService abuseDetection = new AbuseDetectionService(abuse, new CooldownService(), reportRepository);
        ReportValidationService validation = new ReportValidationService(reportSettings, categoryRepository, abuseDetection);
        Executor direct = Runnable::run;
        DiscordWebhookService discord = new DiscordWebhookService(new DiscordSettings(false, "", "", "", false, false, false, false, false, 0L), direct);
        ReportNotificationService notification = new ReportNotificationService(ReportNotificationService.Sink.noop(), List.of());
        ReportCreationService creation = new ReportCreationService(reportSettings, validation, abuseDetection, reportRepository, actionRepository, notification, discord);
        ReportAssignmentService assignment = new ReportAssignmentService(reportRepository, actionRepository, discord);
        ReportSearchService search = new ReportSearchService(reportRepository);
        ReportHistoryService history = new ReportHistoryService(reportRepository);
        ReportStatsService stats = new ReportStatsService(reportRepository, new PlayerStatsRepository(reportRepository), actionRepository);
        ReportExportService export = new ReportExportService(reportSettings, reportRepository, evidenceRepository, noteRepository, actionRepository);
        ReportService service = new ReportService(creation, assignment, validation, search, history, stats, export,
                reportRepository, evidenceRepository, noteRepository, categoryRepository, actionRepository, discord);
        return new Fixtures(databaseManager, categoryRepository, service);
    }

    private record Fixtures(DatabaseManager databaseManager, CategoryRepository categoryRepository, ReportService reportService) implements AutoCloseable {
        @Override
        public void close() {
            databaseManager.close();
        }
    }
}
