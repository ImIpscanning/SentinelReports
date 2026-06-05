package com.imipscanning.sentinelreports.paper.commands;

import java.util.List;
import java.util.Map;

public final class UnclaimReportCommand extends AbstractSubCommand {
    public UnclaimReportCommand() {
        super("unclaim", List.of(), "Libera un reporte asignado.", "sentinelreports.staff.claim",
                "unclaim <id>", List.of("/reports unclaim 15"), "staff");
    }

    @Override
    public CommandResult execute(CommandContext context) {
        long id = requireId(context, 1);
        context.async(() -> context.plugin().reportService().unclaim(id, context.senderUuidOrConsole(), context.senderName()),
                report -> context.reply("success.report_unclaimed", Map.of("id", report.id()), "&aReporte liberado."));
        return CommandResult.SUCCESS;
    }
}
