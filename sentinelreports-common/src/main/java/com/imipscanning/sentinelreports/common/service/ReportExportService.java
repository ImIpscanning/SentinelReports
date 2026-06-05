package com.imipscanning.sentinelreports.common.service;

import com.imipscanning.sentinelreports.common.model.Report;
import com.imipscanning.sentinelreports.common.model.ReportExport;
import com.imipscanning.sentinelreports.common.storage.ActionRepository;
import com.imipscanning.sentinelreports.common.storage.EvidenceRepository;
import com.imipscanning.sentinelreports.common.storage.NoteRepository;
import com.imipscanning.sentinelreports.common.storage.ReportRepository;
import com.imipscanning.sentinelreports.common.util.JsonUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class ReportExportService {
    private final ReportSettings settings;
    private final ReportRepository reportRepository;
    private final EvidenceRepository evidenceRepository;
    private final NoteRepository noteRepository;
    private final ActionRepository actionRepository;

    public ReportExportService(
            ReportSettings settings,
            ReportRepository reportRepository,
            EvidenceRepository evidenceRepository,
            NoteRepository noteRepository,
            ActionRepository actionRepository
    ) {
        this.settings = settings;
        this.reportRepository = reportRepository;
        this.evidenceRepository = evidenceRepository;
        this.noteRepository = noteRepository;
        this.actionRepository = actionRepository;
    }

    public Path export(long reportId, String format) {
        String normalized = format == null ? "" : format.toLowerCase(Locale.ROOT);
        if (!normalized.equals("json") && !normalized.equals("html")) {
            throw new ValidationException("errors.invalid_format", "Unsupported export format");
        }
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ValidationException("errors.invalid_report", "Report not found"));
        ReportExport export = new ReportExport(
                report,
                evidenceRepository.list(reportId),
                noteRepository.list(reportId),
                actionRepository.list(reportId),
                System.currentTimeMillis()
        );
        try {
            Files.createDirectories(settings.exportFolder());
            Path output = settings.exportFolder().resolve("report-" + reportId + "." + normalized);
            if (normalized.equals("json")) {
                JsonUtil.write(output, export);
            } else {
                Files.writeString(output, html(export), StandardCharsets.UTF_8);
            }
            return output;
        } catch (IOException ex) {
            throw new IllegalStateException("Could not export report", ex);
        }
    }

    private String html(ReportExport export) {
        Report report = export.report();
        StringBuilder builder = new StringBuilder("""
                <!doctype html>
                <html lang="es">
                <head>
                  <meta charset="utf-8">
                  <title>SentinelReports Export</title>
                  <style>
                    body{font-family:Inter,Arial,sans-serif;background:#101418;color:#f5f7fb;margin:0;padding:32px}
                    main{max-width:980px;margin:0 auto}
                    section{border:1px solid #2a3440;border-radius:8px;padding:18px;margin:16px 0;background:#151b22}
                    h1,h2{color:#63d8ff} code{color:#a8ffbf}
                    table{width:100%;border-collapse:collapse}td,th{padding:8px;border-bottom:1px solid #2a3440;text-align:left}
                  </style>
                </head>
                <body><main>
                """);
        builder.append("<h1>Reporte #").append(report.id()).append("</h1>");
        builder.append("<section><h2>Resumen</h2><table>");
        row(builder, "Estado", report.status().name());
        row(builder, "Prioridad", report.priority().name());
        row(builder, "Reportado", report.targetName());
        row(builder, "Reportador", report.reporterName());
        row(builder, "Categoria", report.category());
        row(builder, "Servidor", report.serverName());
        row(builder, "Razon", report.reason());
        row(builder, "Cierre", report.closeReason());
        builder.append("</table></section>");
        builder.append("<section><h2>Evidencias</h2><ul>");
        export.evidence().forEach(e -> builder.append("<li><code>").append(escape(e.evidenceType().name())).append("</code> ")
                .append(escape(e.content())).append(" - ").append(escape(e.addedByName())).append("</li>"));
        builder.append("</ul></section>");
        builder.append("<section><h2>Notas internas</h2><ul>");
        export.notes().forEach(n -> builder.append("<li>").append(escape(n.note())).append(" - ").append(escape(n.addedByName())).append("</li>"));
        builder.append("</ul></section>");
        builder.append("<section><h2>Acciones</h2><ul>");
        export.actions().forEach(a -> builder.append("<li><code>").append(escape(a.actionType())).append("</code> ")
                .append(escape(a.actorName())).append(": ").append(escape(a.oldValue())).append(" -> ").append(escape(a.newValue())).append("</li>"));
        builder.append("</ul></section></main></body></html>");
        return builder.toString();
    }

    private void row(StringBuilder builder, String key, String value) {
        builder.append("<tr><th>").append(escape(key)).append("</th><td>").append(escape(value)).append("</td></tr>");
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
