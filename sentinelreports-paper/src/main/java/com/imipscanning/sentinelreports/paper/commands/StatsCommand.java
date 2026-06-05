package com.imipscanning.sentinelreports.paper.commands;

import com.imipscanning.sentinelreports.common.model.ReportStatus;
import com.imipscanning.sentinelreports.common.util.ColorFormatter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;

public final class StatsCommand extends AbstractSubCommand {
    public StatsCommand() {
        super("stats", List.of(), "Muestra estadisticas del sistema o de staff.", "sentinelreports.staff.stats",
                "stats [staff]", List.of("/reports stats"), "staff");
    }

    @Override
    @SuppressWarnings("deprecation")
    public CommandResult execute(CommandContext context) {
        if (!context.arg(1).isBlank()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(context.arg(1));
            context.async(() -> context.plugin().reportService().stats().staff(player.getUniqueId(), player.getName() == null ? context.arg(1) : player.getName()), stats -> {
                context.plugin().messages().line(context.sender());
                context.sender().sendMessage(ColorFormatter.component("&b&lSentinelReports &8» &fStats Staff"));
                context.plugin().messages().line(context.sender());
                context.sender().sendMessage(ColorFormatter.component("&7Staff: &f" + stats.staffName()));
                context.sender().sendMessage(ColorFormatter.component("&7Claimed: &f" + stats.claimed()));
                context.sender().sendMessage(ColorFormatter.component("&7Resolved: &f" + stats.resolved()));
                context.sender().sendMessage(ColorFormatter.component("&7Rejected: &f" + stats.rejected()));
                context.sender().sendMessage(ColorFormatter.component("&7False reports: &f" + stats.falseReports()));
                context.sender().sendMessage(ColorFormatter.component("&7Notes: &f" + stats.notesAdded()));
                context.sender().sendMessage(ColorFormatter.component("&7Evidence: &f" + stats.evidenceAdded()));
                context.plugin().messages().line(context.sender());
            });
            return CommandResult.SUCCESS;
        }
        context.async(() -> context.plugin().reportService().stats().statusCounts(), counts -> {
            context.plugin().messages().line(context.sender());
            context.sender().sendMessage(ColorFormatter.component("&b&lSentinelReports &8» &fStats Sistema"));
            context.plugin().messages().line(context.sender());
            for (ReportStatus status : ReportStatus.values()) {
                context.sender().sendMessage(ColorFormatter.component("&7" + status + ": &f" + counts.getOrDefault(status, 0L)));
            }
            context.plugin().messages().line(context.sender());
        });
        return CommandResult.SUCCESS;
    }
}
