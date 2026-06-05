package com.imipscanning.sentinelreports.paper.commands;

import com.imipscanning.sentinelreports.common.model.Report;
import com.imipscanning.sentinelreports.common.util.ColorFormatter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;

public final class HistoryCommand extends AbstractSubCommand {
    private final Mode mode;

    public HistoryCommand() {
        this("history", Mode.BOTH, "Muestra historial completo por jugador.", "history <player>");
    }

    public HistoryCommand(String name, Mode mode, String description, String usage) {
        super(name, List.of(), description, "sentinelreports.staff.history", usage, List.of("/reports " + name + " Steve"), "history");
        this.mode = mode;
    }

    @Override
    @SuppressWarnings("deprecation")
    public CommandResult execute(CommandContext context) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(context.arg(1));
        if (player.getName() == null && !player.hasPlayedBefore()) {
            context.reply("errors.player_not_found", java.util.Map.of(), "&cJugador no encontrado.");
            return CommandResult.FAILURE;
        }
        context.async(() -> switch (mode) {
            case BY -> context.plugin().reportService().getReportsByPlayer(player.getUniqueId());
            case AGAINST -> context.plugin().reportService().getReportsAgainst(player.getUniqueId());
            case BOTH -> {
                java.util.ArrayList<Report> reports = new java.util.ArrayList<>(context.plugin().reportService().getReportsByPlayer(player.getUniqueId()));
                reports.addAll(context.plugin().reportService().getReportsAgainst(player.getUniqueId()));
                yield reports.stream().sorted(java.util.Comparator.comparingLong(Report::createdAt).reversed()).limit(25).toList();
            }
        }, reports -> send(context, player.getName() == null ? context.arg(1) : player.getName(), reports));
        return CommandResult.SUCCESS;
    }

    private void send(CommandContext context, String player, List<Report> reports) {
        context.plugin().messages().line(context.sender());
        context.sender().sendMessage(ColorFormatter.component("&b&lSentinelReports &8» &fHistorial de " + player));
        context.plugin().messages().line(context.sender());
        if (reports.isEmpty()) {
            context.sender().sendMessage(ColorFormatter.component("&7Sin reportes."));
        }
        for (Report report : reports) {
            context.sender().sendMessage(ColorFormatter.component("&b#" + report.id() + " &8| &7" + report.reporterName()
                    + " &8-> &f" + report.targetName() + " &8| &e" + report.status() + " &8| &7" + report.closeReason()));
        }
        context.plugin().messages().line(context.sender());
    }

    @Override
    public List<String> tabComplete(CommandContext context) {
        if (context.args().length == 2) {
            return context.onlinePlayerNames(context.arg(1));
        }
        return List.of();
    }

    public enum Mode {
        BY,
        AGAINST,
        BOTH
    }
}
