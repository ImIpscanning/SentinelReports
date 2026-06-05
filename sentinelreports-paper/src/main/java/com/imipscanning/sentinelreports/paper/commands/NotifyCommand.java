package com.imipscanning.sentinelreports.paper.commands;

import java.util.List;
import java.util.Map;

public final class NotifyCommand extends AbstractSubCommand {
    public NotifyCommand() {
        super("notify", List.of(), "Reenvia un reporte a Discord.", "sentinelreports.staff.discord",
                "notify <id>", List.of("/reports notify 15"), "staff");
    }

    @Override
    public CommandResult execute(CommandContext context) {
        long id = requireId(context, 1);
        context.async(() -> context.plugin().reportService().getReportById(id).orElse(null), report -> {
            if (report == null) {
                context.reply("errors.invalid_report", Map.of(), "&cEse reporte no existe.");
                return;
            }
            context.plugin().reportService().discord().sendNewReport(report);
            context.reply("success.discord_test", Map.of(), "&aNotificacion enviada.");
        });
        return CommandResult.SUCCESS;
    }
}
