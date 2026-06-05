package com.imipscanning.sentinelreports.paper.commands;

import com.imipscanning.sentinelreports.common.model.ReportPriority;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class PriorityReportCommand extends AbstractSubCommand {
    public PriorityReportCommand() {
        super("priority", List.of(), "Cambia la prioridad de un reporte.", "sentinelreports.staff.close",
                "priority <id> <priority>", List.of("/reports priority 15 CRITICAL"), "staff");
    }

    @Override
    public CommandResult execute(CommandContext context) {
        long id = requireId(context, 1);
        ReportPriority priority = ReportPriority.parse(context.arg(2))
                .orElseThrow(() -> new CommandException("errors.invalid_priority", "Invalid priority", syntax().usage(), syntax().examples().getFirst()));
        context.async(() -> context.plugin().reportService().updatePriority(id, priority, context.senderUuidOrConsole(), context.senderName()),
                report -> context.reply("success.priority_changed", Map.of("id", report.id(), "priority", report.priority()), "&aPrioridad actualizada."));
        return CommandResult.SUCCESS;
    }

    @Override
    public List<String> tabComplete(CommandContext context) {
        if (context.args().length == 3) {
            return Arrays.stream(ReportPriority.values()).map(Enum::name).toList();
        }
        return List.of();
    }
}
