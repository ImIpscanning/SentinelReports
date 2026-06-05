package com.imipscanning.sentinelreports.common.service;

import com.imipscanning.sentinelreports.common.model.ReportFilter;
import com.imipscanning.sentinelreports.common.model.ReportStatus;
import com.imipscanning.sentinelreports.common.storage.ReportRepository;

import java.util.UUID;

public final class PlaceholderService {
    private final ReportRepository reportRepository;

    public PlaceholderService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public String openReports() {
        return String.valueOf(reportRepository.count(new ReportFilter(ReportStatus.OPEN, null, null, null, null, null, null, 0L, 0L)));
    }

    public String assignedReports(UUID staffUuid) {
        return String.valueOf(reportRepository.count(new ReportFilter(null, null, null, null, null, null, staffUuid, 0L, 0L)));
    }

    public String playerReports(UUID playerUuid) {
        return String.valueOf(reportRepository.count(new ReportFilter(null, null, null, null, playerUuid, null, null, 0L, 0L)));
    }

    public String playerFalseReports(UUID playerUuid) {
        return String.valueOf(reportRepository.count(new ReportFilter(ReportStatus.FALSE_REPORT, null, null, null, playerUuid, null, null, 0L, 0L)));
    }
}
