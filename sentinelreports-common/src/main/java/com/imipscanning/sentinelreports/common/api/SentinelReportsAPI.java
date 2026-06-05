package com.imipscanning.sentinelreports.common.api;

import com.imipscanning.sentinelreports.common.model.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SentinelReportsAPI {
    Report createReport(ReportCreateRequest request);

    Report closeReport(long reportId, ReportStatus status, UUID actorUuid, String actorName, String reason);

    ReportEvidence addEvidence(long reportId, String content, UUID actorUuid, String actorName);

    ReportNote addNote(long reportId, String note, UUID actorUuid, String actorName);

    Optional<Report> getReportById(long id);

    List<Report> getReportsByPlayer(UUID playerUuid);

    List<Report> getOpenReports();

    void registerCategory(ReportCategory category);

    PlayerReportStats getPlayerReportStats(UUID playerUuid);
}
