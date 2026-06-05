package com.imipscanning.sentinelreports.common.service;

import com.imipscanning.sentinelreports.common.model.Report;
import com.imipscanning.sentinelreports.common.model.ReportFilter;
import com.imipscanning.sentinelreports.common.model.ReportQuery;
import com.imipscanning.sentinelreports.common.storage.ReportRepository;

import java.util.List;
import java.util.UUID;

public final class ReportHistoryService {
    private final ReportRepository reportRepository;

    public ReportHistoryService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public List<Report> reportsBy(UUID playerUuid, int limit) {
        return reportRepository.list(new ReportQuery(new ReportFilter(null, null, null, null, playerUuid, null, null, 0L, 0L),
                1, limit, "created_at", true));
    }

    public List<Report> reportsAgainst(UUID playerUuid, int limit) {
        return reportRepository.list(new ReportQuery(new ReportFilter(null, null, null, null, null, playerUuid, null, 0L, 0L),
                1, limit, "created_at", true));
    }
}
