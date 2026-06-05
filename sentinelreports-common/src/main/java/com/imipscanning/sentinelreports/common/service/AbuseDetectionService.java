package com.imipscanning.sentinelreports.common.service;

import com.imipscanning.sentinelreports.common.model.ReportCategory;
import com.imipscanning.sentinelreports.common.model.ReportCreateRequest;
import com.imipscanning.sentinelreports.common.storage.ReportRepository;

import java.util.UUID;

public final class AbuseDetectionService {
    private static final String GLOBAL_SCOPE = "report:global";
    private static final String TARGET_SCOPE_PREFIX = "report:target:";

    private final AbuseProtectionSettings settings;
    private final CooldownService cooldownService;
    private final ReportRepository reportRepository;

    public AbuseDetectionService(AbuseProtectionSettings settings, CooldownService cooldownService, ReportRepository reportRepository) {
        this.settings = settings;
        this.cooldownService = cooldownService;
        this.reportRepository = reportRepository;
    }

    public AbuseCheckResult check(ReportCreateRequest request, ReportCategory category) {
        if (!settings.enabled()) {
            return AbuseCheckResult.ok();
        }
        if (settings.blockSelfReports() && request.reporterUuid().equals(request.targetUuid())) {
            return AbuseCheckResult.denied("errors.self_report", 0L);
        }
        if (!settings.allowReportingStaff() && request.targetIsStaff()) {
            return AbuseCheckResult.denied("errors.no_permission", 0L);
        }
        if (!request.bypassCooldown()) {
            long global = cooldownService.remainingMillis(GLOBAL_SCOPE, request.reporterUuid());
            if (global > 0) {
                return AbuseCheckResult.denied("errors.cooldown", global);
            }
            long sameTarget = cooldownService.remainingMillis(targetScope(request.targetUuid()), request.reporterUuid());
            if (sameTarget > 0) {
                return AbuseCheckResult.denied("errors.cooldown", sameTarget);
            }
        }
        if (!request.bypassLimit() && settings.maxOpenReportsPerPlayer() > 0
                && reportRepository.countOpenByReporter(request.reporterUuid()) >= settings.maxOpenReportsPerPlayer()) {
            return AbuseCheckResult.denied("errors.max_open_reports", 0L);
        }
        if (settings.blockDuplicateReports()) {
            long after = System.currentTimeMillis() - settings.duplicateWindowMillis();
            if (reportRepository.hasRecentDuplicate(request.reporterUuid(), request.targetUuid(), request.category(), after)) {
                return AbuseCheckResult.denied("errors.duplicate_report", 0L);
            }
        }
        return AbuseCheckResult.ok();
    }

    public void markCreated(ReportCreateRequest request, ReportCategory category) {
        if (!settings.enabled() || request.bypassCooldown()) {
            return;
        }
        cooldownService.mark(GLOBAL_SCOPE, request.reporterUuid(), settings.globalCooldownMillis());
        long targetCooldown = category.cooldownMillis() > 0 ? category.cooldownMillis() : settings.sameTargetCooldownMillis();
        cooldownService.mark(targetScope(request.targetUuid()), request.reporterUuid(), targetCooldown);
    }

    private String targetScope(UUID targetUuid) {
        return TARGET_SCOPE_PREFIX + targetUuid;
    }

    public record AbuseCheckResult(boolean allowed, String messageKey, long remainingMillis) {
        public static AbuseCheckResult ok() {
            return new AbuseCheckResult(true, "", 0L);
        }

        public static AbuseCheckResult denied(String messageKey, long remainingMillis) {
            return new AbuseCheckResult(false, messageKey, remainingMillis);
        }
    }
}
