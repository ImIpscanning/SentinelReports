package com.imipscanning.sentinelreports.common.service;

import com.imipscanning.sentinelreports.common.model.ReportCategory;
import com.imipscanning.sentinelreports.common.model.ReportCreateRequest;
import com.imipscanning.sentinelreports.common.model.ReportPriority;
import com.imipscanning.sentinelreports.common.model.ReportStatus;
import com.imipscanning.sentinelreports.common.storage.CategoryRepository;
import com.imipscanning.sentinelreports.common.util.StringSanitizer;

public final class ReportValidationService {
    private final ReportSettings reportSettings;
    private final CategoryRepository categoryRepository;
    private final AbuseDetectionService abuseDetectionService;

    public ReportValidationService(ReportSettings reportSettings, CategoryRepository categoryRepository, AbuseDetectionService abuseDetectionService) {
        this.reportSettings = reportSettings;
        this.categoryRepository = categoryRepository;
        this.abuseDetectionService = abuseDetectionService;
    }

    public ReportCategory validateCreate(ReportCreateRequest request) {
        if (request.reporterUuid() == null || request.targetUuid() == null) {
            throw new ValidationException("errors.player_not_found", "Reporter and target are required");
        }
        if (reportSettings.requireReason() && StringSanitizer.reason(request.reason()).isBlank()) {
            throw new ValidationException("errors.reason_required", "Reason is required");
        }
        ReportCategory category = categoryRepository.find(request.category())
                .orElseThrow(() -> new ValidationException("errors.invalid_category", "Unknown category"));
        AbuseDetectionService.AbuseCheckResult abuse = abuseDetectionService.check(request, category);
        if (!abuse.allowed()) {
            throw new ValidationException(abuse.messageKey(), String.valueOf(abuse.remainingMillis()));
        }
        return category;
    }

    public ReportStatus requireStatus(String input) {
        return ReportStatus.parse(input).orElseThrow(() -> new ValidationException("errors.invalid_status", "Invalid status"));
    }

    public ReportPriority requirePriority(String input) {
        return ReportPriority.parse(input).orElseThrow(() -> new ValidationException("errors.invalid_priority", "Invalid priority"));
    }

    public void requireCloseReason(String reason) {
        if (reportSettings.closeRequiresReason() && StringSanitizer.reason(reason).isBlank()) {
            throw new ValidationException("errors.reason_required", "Close reason is required");
        }
    }
}
