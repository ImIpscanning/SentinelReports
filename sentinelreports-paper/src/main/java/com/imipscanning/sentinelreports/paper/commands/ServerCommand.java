package com.imipscanning.sentinelreports.paper.commands;

import java.util.List;
import java.util.Map;

public final class ServerCommand extends AbstractSubCommand {
    public ServerCommand() {
        super("server", List.of(), "Muestra el servidor asociado a un reporte.", "sentinelreports.staff.view",
                "server <id>", List.of("/reports server 15"), "staff");
    }

    @Override
    public CommandResult execute(CommandContext context) {
        long id = requireId(context, 1);
        context.async(() -> context.plugin().reportService().getReportById(id).orElse(null), report -> {
            if (report == null) {
                context.reply("errors.invalid_report", Map.of(), "&cEse reporte no existe.");
                return;
            }
            context.reply("report.server", Map.of("id", id, "server", report.serverName()), "&7Servidor del reporte &f#" + id + "&7: &f" + report.serverName());
        });
        return CommandResult.SUCCESS;
    }
}
