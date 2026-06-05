package com.imipscanning.sentinelreports.paper.commands;

import com.imipscanning.sentinelreports.common.model.Report;
import com.imipscanning.sentinelreports.common.util.ColorFormatter;
import com.imipscanning.sentinelreports.common.util.TimeFormatter;

import java.util.List;
import java.util.Map;

public final class ViewReportCommand extends AbstractSubCommand {
    public ViewReportCommand() {
        super("view", List.of(), "Mira la informacion completa de un reporte.", "sentinelreports.staff.view",
                "view <id>", List.of("/reports view 15"), "staff");
    }

    @Override
    public CommandResult execute(CommandContext context) {
        long id = requireId(context, 1);
        context.async(() -> context.plugin().reportService().getReportById(id).orElse(null), report -> {
            if (report == null) {
                context.reply("errors.invalid_report", Map.of(), "&cEse reporte no existe.");
                return;
            }
            send(context, report);
        });
        return CommandResult.SUCCESS;
    }

    public static void send(CommandContext context, Report report) {
        context.plugin().messages().line(context.sender());
        context.sender().sendMessage(ColorFormatter.component("&b&lSentinelReports &8» &fReporte #" + report.id()));
        context.plugin().messages().line(context.sender());
        context.sender().sendMessage(ColorFormatter.component("&7Reportado: &f" + report.targetName()));
        context.sender().sendMessage(ColorFormatter.component("&7Reportador: &f" + report.reporterName()));
        context.sender().sendMessage(ColorFormatter.component("&7Categoria: &f" + report.category()));
        context.sender().sendMessage(ColorFormatter.component("&7Prioridad: &f" + report.priority()));
        context.sender().sendMessage(ColorFormatter.component("&7Estado: &f" + report.status()));
        context.sender().sendMessage(ColorFormatter.component("&7Servidor: &f" + report.serverName()));
        context.sender().sendMessage(ColorFormatter.component("&7Fecha: &f" + TimeFormatter.since(report.createdAt())));
        context.sender().sendMessage(ColorFormatter.component("&7Razon: &f" + report.reason()));
        context.plugin().messages().line(context.sender());
        context.sender().sendMessage(ColorFormatter.component("&bAcciones:"));
        context.sender().sendMessage(ColorFormatter.component("&f/reports claim " + report.id()));
        context.sender().sendMessage(ColorFormatter.component("&f/reports close " + report.id() + " <reason>"));
        context.sender().sendMessage(ColorFormatter.component("&f/reports evidence list " + report.id()));
        context.plugin().messages().line(context.sender());
    }

    @Override
    public List<String> tabComplete(CommandContext context) {
        return List.of();
    }
}
