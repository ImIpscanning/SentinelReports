package com.imipscanning.sentinelreports.paper.commands;

import com.imipscanning.sentinelreports.common.model.ReportEvidence;
import com.imipscanning.sentinelreports.common.util.ColorFormatter;

import java.util.List;
import java.util.Map;

public final class EvidenceCommand extends AbstractSubCommand {
    public EvidenceCommand() {
        super("evidence", List.of(), "Gestiona evidencias de un reporte.", "sentinelreports.staff.evidence",
                "evidence <add|list|remove> <id> [text|evidenceId]", List.of("/reports evidence add 15 https://example.com/video"), "evidence");
    }

    @Override
    public CommandResult execute(CommandContext context) {
        String action = context.arg(1).toLowerCase();
        long reportId = requireId(context, 2);
        switch (action) {
            case "add" -> {
                String content = context.joined(3);
                context.async(() -> context.plugin().reportService().addEvidence(reportId, content, context.senderUuidOrConsole(), context.senderName()),
                        evidence -> context.reply("success.evidence_added", Map.of("id", reportId), "&aEvidencia anadida."));
            }
            case "list" -> context.async(() -> context.plugin().reportService().evidence(reportId), evidence -> sendList(context, reportId, evidence));
            case "remove" -> {
                long evidenceId = CommandArgumentParser.reportId(context.arg(3))
                        .orElseThrow(() -> new CommandException("errors.invalid_report", "Invalid evidence id", syntax().usage(), syntax().examples().getFirst()));
                context.async(() -> context.plugin().reportService().removeEvidence(reportId, evidenceId, context.senderUuidOrConsole(), context.senderName()),
                        removed -> context.reply("success.evidence_removed", Map.of("id", reportId, "evidence_id", evidenceId), removed ? "&aEvidencia eliminada." : "&cEvidencia no encontrada."));
            }
            default -> throw new CommandException("errors.invalid_command", "Invalid evidence action", syntax().usage(), syntax().examples().getFirst());
        }
        return CommandResult.SUCCESS;
    }

    private void sendList(CommandContext context, long reportId, List<ReportEvidence> evidence) {
        context.plugin().messages().line(context.sender());
        context.sender().sendMessage(ColorFormatter.component("&b&lSentinelReports &8» &fEvidencias #" + reportId));
        context.plugin().messages().line(context.sender());
        if (evidence.isEmpty()) {
            context.sender().sendMessage(ColorFormatter.component("&7No hay evidencias."));
        }
        for (ReportEvidence item : evidence) {
            context.sender().sendMessage(ColorFormatter.component("&b#" + item.id() + " &8| &f" + item.evidenceType() + " &8| &7" + item.content()));
        }
        context.plugin().messages().line(context.sender());
    }

    @Override
    public List<String> tabComplete(CommandContext context) {
        if (context.args().length == 2) {
            return List.of("add", "list", "remove");
        }
        return List.of();
    }
}
