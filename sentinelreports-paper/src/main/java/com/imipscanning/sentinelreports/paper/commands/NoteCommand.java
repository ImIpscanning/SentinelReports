package com.imipscanning.sentinelreports.paper.commands;

import com.imipscanning.sentinelreports.common.model.ReportNote;
import com.imipscanning.sentinelreports.common.util.ColorFormatter;

import java.util.List;
import java.util.Map;

public final class NoteCommand extends AbstractSubCommand {
    public NoteCommand() {
        super("note", List.of(), "Gestiona notas internas de staff.", "sentinelreports.staff.note",
                "note <add|list> <id> [note]", List.of("/reports note add 15 Revisar logs"), "notes");
    }

    @Override
    public CommandResult execute(CommandContext context) {
        String action = context.arg(1).toLowerCase();
        long reportId = requireId(context, 2);
        switch (action) {
            case "add" -> {
                String note = context.joined(3);
                context.async(() -> context.plugin().reportService().addNote(reportId, note, context.senderUuidOrConsole(), context.senderName()),
                        saved -> context.reply("success.note_added", Map.of("id", reportId), "&aNota anadida."));
            }
            case "list" -> context.async(() -> context.plugin().reportService().notes(reportId), notes -> sendList(context, reportId, notes));
            default -> throw new CommandException("errors.invalid_command", "Invalid note action", syntax().usage(), syntax().examples().getFirst());
        }
        return CommandResult.SUCCESS;
    }

    private void sendList(CommandContext context, long reportId, List<ReportNote> notes) {
        context.plugin().messages().line(context.sender());
        context.sender().sendMessage(ColorFormatter.component("&b&lSentinelReports &8» &fNotas #" + reportId));
        context.plugin().messages().line(context.sender());
        if (notes.isEmpty()) {
            context.sender().sendMessage(ColorFormatter.component("&7No hay notas internas."));
        }
        for (ReportNote note : notes) {
            context.sender().sendMessage(ColorFormatter.component("&b#" + note.id() + " &8| &f" + note.addedByName() + " &8» &7" + note.note()));
        }
        context.plugin().messages().line(context.sender());
    }

    @Override
    public List<String> tabComplete(CommandContext context) {
        if (context.args().length == 2) {
            return List.of("add", "list");
        }
        return List.of();
    }
}
