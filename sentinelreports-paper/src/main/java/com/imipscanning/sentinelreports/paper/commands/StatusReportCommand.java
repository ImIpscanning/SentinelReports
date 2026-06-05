package com.imipscanning.sentinelreports.paper.commands;

import com.imipscanning.sentinelreports.common.model.ReportStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class StatusReportCommand extends AbstractSubCommand {
    public StatusReportCommand() {
        super("status", List.of(), "Cambia el estado de un reporte.", "sentinelreports.staff.close",
                "status <id> <status>", List.of("/reports status 15 REVIEWING"), "staff");
    }

    @Override
    public CommandResult execute(CommandContext context) {
        long id = requireId(context, 1);
        ReportStatus status = ReportStatus.parse(context.arg(2))
                .orElseThrow(() -> new CommandException("errors.invalid_status", "Invalid status", syntax().usage(), syntax().examples().getFirst()));
        context.async(() -> context.plugin().reportService().updateStatus(id, status, context.senderUuidOrConsole(), context.senderName()),
                report -> context.reply("success.status_changed", Map.of("id", report.id(), "status", report.status()), "&aEstado actualizado."));
        return CommandResult.SUCCESS;
    }

    @Override
    public List<String> tabComplete(CommandContext context) {
        if (context.args().length == 3) {
            return Arrays.stream(ReportStatus.values()).map(Enum::name).toList();
        }
        return List.of();
    }
}
