package com.imipscanning.sentinelreports.paper.commands;

import java.util.List;
import java.util.Map;

public final class ClaimReportCommand extends AbstractSubCommand {
    public ClaimReportCommand() {
        super("claim", List.of(), "Toma un reporte para revisarlo.", "sentinelreports.staff.claim",
                "claim <id>", List.of("/reports claim 15"), "staff");
    }

    @Override
    public CommandResult execute(CommandContext context) {
        long id = requireId(context, 1);
        context.async(() -> context.plugin().reportService().claim(id, context.senderUuidOrConsole(), context.senderName()),
                report -> context.reply("success.report_claimed", Map.of("id", report.id()), "&aTomaste el reporte &f#" + report.id()));
        return CommandResult.SUCCESS;
    }
}
