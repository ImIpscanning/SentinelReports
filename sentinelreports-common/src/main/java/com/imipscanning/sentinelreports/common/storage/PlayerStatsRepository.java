package com.imipscanning.sentinelreports.common.storage;

import com.imipscanning.sentinelreports.common.model.PlayerReportStats;

import java.util.UUID;

public final class PlayerStatsRepository {
    private final ReportRepository reportRepository;

    public PlayerStatsRepository(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public PlayerReportStats get(UUID playerUuid, String playerName) {
        return reportRepository.playerStats(playerUuid, playerName);
    }
}
