package com.imipscanning.sentinelreports.paper.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public final class AssignReportCommand extends AbstractSubCommand {
    public AssignReportCommand() {
        super("assign", List.of(), "Asigna un reporte a un staff.", "sentinelreports.staff.assign",
                "assign <id> <staff>", List.of("/reports assign 15 Moderator"), "staff");
    }

    @Override
    public CommandResult execute(CommandContext context) {
        long id = requireId(context, 1);
        Player staff = Bukkit.getPlayerExact(context.arg(2));
        if (staff == null) {
            context.reply("errors.player_not_found", Map.of(), "&cJugador no encontrado.");
            return CommandResult.FAILURE;
        }
        context.async(() -> context.plugin().reportService().assign(id, staff.getUniqueId(), staff.getName(), context.senderUuidOrConsole(), context.senderName()),
                report -> context.reply("success.report_assigned", Map.of("id", report.id(), "staff", staff.getName()), "&aReporte asignado."));
        return CommandResult.SUCCESS;
    }

    @Override
    public List<String> tabComplete(CommandContext context) {
        if (context.args().length == 3) {
            return context.onlinePlayerNames(context.arg(2));
        }
        return List.of();
    }
}
