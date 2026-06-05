package com.imipscanning.sentinelreports.common.service;

import com.imipscanning.sentinelreports.common.model.PlayerReportStats;
import com.imipscanning.sentinelreports.common.model.ReportFilter;
import com.imipscanning.sentinelreports.common.model.ReportStatus;
import com.imipscanning.sentinelreports.common.model.StaffReportStats;
import com.imipscanning.sentinelreports.common.storage.ActionRepository;
import com.imipscanning.sentinelreports.common.storage.PlayerStatsRepository;
import com.imipscanning.sentinelreports.common.storage.ReportRepository;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public final class ReportStatsService {
    private final ReportRepository reportRepository;
    private final PlayerStatsRepository playerStatsRepository;
    private final ActionRepository actionRepository;

    public ReportStatsService(ReportRepository reportRepository, PlayerStatsRepository playerStatsRepository, ActionRepository actionRepository) {
        this.reportRepository = reportRepository;
        this.playerStatsRepository = playerStatsRepository;
        this.actionRepository = actionRepository;
    }

    public PlayerReportStats player(UUID uuid, String name) {
        return playerStatsRepository.get(uuid, name);
    }

    public StaffReportStats staff(UUID uuid, String name) {
        return actionRepository.staffStats(uuid, name);
    }

    public Map<ReportStatus, Long> statusCounts() {
        Map<ReportStatus, Long> counts = new EnumMap<>(ReportStatus.class);
        for (ReportStatus status : ReportStatus.values()) {
            counts.put(status, reportRepository.count(new ReportFilter(status, null, null, null, null, null, null, 0L, 0L)));
        }
        return counts;
    }
}
