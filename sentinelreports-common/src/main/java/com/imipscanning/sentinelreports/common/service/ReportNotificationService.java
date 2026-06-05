package com.imipscanning.sentinelreports.common.service;

import com.imipscanning.sentinelreports.common.model.Report;
import com.imipscanning.sentinelreports.common.util.PlaceholderFormatter;

import java.util.List;
import java.util.Map;

public final class ReportNotificationService {
    private final Sink sink;
    private final List<String> newReportTemplate;

    public ReportNotificationService(Sink sink, List<String> newReportTemplate) {
        this.sink = sink == null ? Sink.noop() : sink;
        this.newReportTemplate = newReportTemplate == null ? List.of() : List.copyOf(newReportTemplate);
    }

    public void notifyNewReport(Report report) {
        Map<String, Object> placeholders = Map.of(
                "id", report.id(),
                "target", report.targetName(),
                "reporter", report.reporterName(),
                "category", report.category(),
                "priority", report.priority(),
                "server", report.serverName(),
                "reason", report.reason()
        );
        List<String> lines = newReportTemplate.stream()
                .map(line -> PlaceholderFormatter.format(line, placeholders))
                .toList();
        sink.notifyStaff(lines);
    }

    public interface Sink {
        void notifyStaff(List<String> lines);

        static Sink noop() {
            return lines -> {
            };
        }
    }
}
