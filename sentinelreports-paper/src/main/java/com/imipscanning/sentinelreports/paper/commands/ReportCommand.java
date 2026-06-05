package com.imipscanning.sentinelreports.paper.commands;

import com.imipscanning.sentinelreports.common.model.ReportCreateRequest;
import com.imipscanning.sentinelreports.common.model.ReportFilter;
import com.imipscanning.sentinelreports.common.model.ReportQuery;
import com.imipscanning.sentinelreports.common.model.ReportStatus;
import com.imipscanning.sentinelreports.common.util.ColorFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class ReportCommand implements CommandExecutor, TabCompleter {
    private final com.imipscanning.sentinelreports.paper.SentinelReportsPaperPlugin plugin;

    public ReportCommand(com.imipscanning.sentinelreports.paper.SentinelReportsPaperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        CommandContext context = new CommandContext(plugin, sender, label, args);
        try {
            if (args.length == 0) {
                plugin.guiManager().openPlayerMain(context.player());
                return true;
            }
            switch (args[0].toLowerCase()) {
                case "help" -> sendHelp(context);
                case "cancel" -> plugin.sessionManager().cancel(context.player());
                case "status" -> sendStatus(context);
                case "history" -> sendHistory(context);
                default -> handleCreate(context);
            }
        } catch (CommandException ex) {
            plugin.messages().send(sender, ex.messageKey(), Map.of(), "&c" + ex.getMessage());
        }
        return true;
    }

    private void handleCreate(CommandContext context) {
        context.requirePermission("sentinelreports.report");
        Player reporter = context.player();
        Player target = Bukkit.getPlayerExact(context.arg(0));
        if (target == null) {
            context.reply("errors.player_not_found", Map.of(), "&cJugador no encontrado.");
            return;
        }
        if (context.args().length == 1) {
            plugin.guiManager().openCategory(reporter, target);
            return;
        }
        if (context.args().length < 3) {
            sendHelp(context);
            return;
        }
        String category = context.arg(1);
        String reason = context.joined(2);
        Location location = target.getLocation();
        ReportCreateRequest request = new ReportCreateRequest(
                reporter.getUniqueId(),
                reporter.getName(),
                target.getUniqueId(),
                target.getName(),
                category,
                reason,
                plugin.reportSettings().serverName(),
                location.getWorld() == null ? null : location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                target.hasPermission("sentinelreports.staff"),
                reporter.hasPermission("sentinelreports.bypass.cooldown"),
                reporter.hasPermission("sentinelreports.bypass.limit")
        );
        context.async(() -> plugin.reportService().createReport(request), report -> context.replyList("report.created_panel", Map.of(
                "id", report.id(),
                "target", report.targetName(),
                "category", report.category(),
                "priority", report.priority(),
                "reason", report.reason()
        ), List.of("&aReporte creado. ID: &f#" + report.id())));
    }

    private void sendHelp(CommandContext context) {
        plugin.messages().line(context.sender());
        context.sender().sendMessage(ColorFormatter.component("&b&lSentinelReports &8» &fAyuda"));
        plugin.messages().line(context.sender());
        context.sender().sendMessage(ColorFormatter.component("&b/report &7Abre el menu de reportes."));
        context.sender().sendMessage(ColorFormatter.component("&b/report <player> &7Abre categorias para reportar."));
        context.sender().sendMessage(ColorFormatter.component("&b/report <player> <category> <reason> &7Crea reporte directo."));
        context.sender().sendMessage(ColorFormatter.component("&b/report status &7Mira tus reportes abiertos."));
        context.sender().sendMessage(ColorFormatter.component("&b/report history &7Mira tus reportes enviados."));
        plugin.messages().line(context.sender());
    }

    private void sendStatus(CommandContext context) {
        Player player = context.player();
        context.async(() -> plugin.reportService().query(new ReportQuery(
                new ReportFilter(ReportStatus.OPEN, null, null, null, player.getUniqueId(), null, null, 0L, 0L),
                1, 10, "created_at", true)), page -> {
            plugin.messages().line(context.sender());
            context.sender().sendMessage(ColorFormatter.component("&b&lSentinelReports &8» &fMis reportes abiertos"));
            plugin.messages().line(context.sender());
            if (page.items().isEmpty()) {
                context.sender().sendMessage(ColorFormatter.component("&7No tienes reportes abiertos."));
            }
            page.items().forEach(report -> context.sender().sendMessage(ColorFormatter.component("&b#" + report.id() + " &8| &f" + report.targetName()
                    + " &8| &7" + report.category() + " &8| &e" + report.status())));
            plugin.messages().line(context.sender());
        });
    }

    private void sendHistory(CommandContext context) {
        Player player = context.player();
        context.async(() -> plugin.reportService().getReportsByPlayer(player.getUniqueId()), reports -> {
            plugin.messages().line(context.sender());
            context.sender().sendMessage(ColorFormatter.component("&b&lSentinelReports &8» &fMi historial"));
            plugin.messages().line(context.sender());
            if (reports.isEmpty()) {
                context.sender().sendMessage(ColorFormatter.component("&7No has creado reportes."));
            }
            reports.stream().limit(10).forEach(report -> context.sender().sendMessage(ColorFormatter.component("&b#" + report.id()
                    + " &8| &f" + report.targetName() + " &8| &e" + report.status())));
            plugin.messages().line(context.sender());
        });
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> base = new java.util.ArrayList<>(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
            base.addAll(List.of("help", "cancel", "status", "history"));
            return filter(base, args[0]);
        }
        if (args.length == 2) {
            return filter(plugin.reportService().categories().stream().map(com.imipscanning.sentinelreports.common.model.ReportCategory::id).toList(), args[1]);
        }
        return List.of();
    }

    private List<String> filter(List<String> values, String prefix) {
        String lower = prefix == null ? "" : prefix.toLowerCase();
        return values.stream().filter(value -> value.toLowerCase().startsWith(lower)).sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }
}
