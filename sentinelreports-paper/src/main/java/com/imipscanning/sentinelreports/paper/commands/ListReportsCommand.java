package com.imipscanning.sentinelreports.paper.commands;

import com.imipscanning.sentinelreports.common.model.Report;
import com.imipscanning.sentinelreports.common.model.ReportFilter;
import com.imipscanning.sentinelreports.common.model.ReportQuery;
import com.imipscanning.sentinelreports.common.model.ReportStatus;
import com.imipscanning.sentinelreports.common.util.ColorFormatter;
import com.imipscanning.sentinelreports.common.util.TimeFormatter;

import java.util.List;
import java.util.Map;

public final class ListReportsCommand extends AbstractSubCommand {
    public ListReportsCommand() {
        super("list", List.of(), "Lista reportes con filtros de estado.", "sentinelreports.staff.list",
                "list [open|assigned|closed|status] [page]", List.of("/reports list open"), "staff");
    }

    @Override
    public CommandResult execute(CommandContext context) {
        String filter = context.arg(1);
        int page = parsePage(context.arg(2));
        ReportStatus status = switch (filter.toLowerCase()) {
            case "open" -> ReportStatus.OPEN;
            case "assigned" -> ReportStatus.ASSIGNED;
            case "closed" -> ReportStatus.CLOSED;
            case "" -> null;
            default -> ReportStatus.parse(filter).orElse(null);
        };
        context.async(() -> context.plugin().reportService().query(new ReportQuery(
                new ReportFilter(status, null, null, null, null, null, null, 0L, 0L),
                page, 8, "created_at", true)), result -> {
            context.plugin().messages().line(context.sender());
            context.sender().sendMessage(ColorFormatter.component("&b&lSentinelReports &8» &fReportes"));
            context.plugin().messages().line(context.sender());
            if (result.items().isEmpty()) {
                context.sender().sendMessage(ColorFormatter.component("&7No hay reportes para este filtro."));
            }
            for (Report report : result.items()) {
                context.sender().sendMessage(ColorFormatter.component("&b#" + report.id() + " &8| &f" + report.targetName()
                        + " &8<- &7" + report.reporterName() + " &8| &f" + report.category()
                        + " &8| &e" + report.priority() + " &8| &7" + TimeFormatter.since(report.createdAt())));
            }
            context.sender().sendMessage(ColorFormatter.component("&7Pagina &f" + result.page() + "&7/&f" + result.totalPages()));
            context.plugin().messages().line(context.sender());
        });
        return CommandResult.SUCCESS;
    }

    @Override
    public List<String> tabComplete(CommandContext context) {
        if (context.args().length == 2) {
            return List.of("open", "assigned", "closed", "OPEN", "REVIEWING", "WAITING_EVIDENCE", "RESOLVED", "REJECTED", "FALSE_REPORT");
        }
        return List.of();
    }

    private int parsePage(String input) {
        try {
            return Math.max(1, Integer.parseInt(input));
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }
}
