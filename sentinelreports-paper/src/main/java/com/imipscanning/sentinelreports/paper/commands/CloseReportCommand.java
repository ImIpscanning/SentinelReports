package com.imipscanning.sentinelreports.paper.commands;

import com.imipscanning.sentinelreports.common.model.ReportStatus;

import java.util.List;
import java.util.Map;

public class CloseReportCommand extends AbstractSubCommand {
    private final ReportStatus closeStatus;

    public CloseReportCommand() {
        this("close", "Cierra un reporte como resuelto.", "sentinelreports.staff.close", ReportStatus.RESOLVED,
                "close <id> <reason>", List.of("/reports close 15 Revisado y resuelto"));
    }

    protected CloseReportCommand(String name, String description, String permission, ReportStatus closeStatus, String usage, List<String> examples) {
        super(name, List.of(), description, permission, usage, examples, "staff");
        this.closeStatus = closeStatus;
    }

    @Override
    public CommandResult execute(CommandContext context) {
        long id = requireId(context, 1);
        String reason = context.joined(2);
        context.async(() -> context.plugin().reportService().closeReport(id, closeStatus, context.senderUuidOrConsole(), context.senderName(), reason),
                report -> context.reply("success.report_closed", Map.of("id", report.id()), "&aReporte cerrado correctamente."));
        return CommandResult.SUCCESS;
    }
}
