package com.imipscanning.sentinelreports.velocity.commands;

import com.imipscanning.sentinelreports.common.model.*;
import com.imipscanning.sentinelreports.common.service.ValidationException;
import com.imipscanning.sentinelreports.common.util.CommandArgumentParser;
import com.imipscanning.sentinelreports.common.util.TimeFormatter;
import com.imipscanning.sentinelreports.velocity.SentinelReportsVelocityPlugin;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class VelocityReportsCommand implements SimpleCommand {
    private final SentinelReportsVelocityPlugin plugin;

    public VelocityReportsCommand(SentinelReportsVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            help(invocation);
            return;
        }
        try {
            switch (args[0].toLowerCase()) {
                case "list" -> list(invocation, args);
                case "view" -> view(invocation, args);
                case "claim" -> claim(invocation, args);
                case "unclaim" -> unclaim(invocation, args);
                case "assign" -> assign(invocation, args);
                case "status" -> status(invocation, args);
                case "priority" -> priority(invocation, args);
                case "close" -> close(invocation, args, ReportStatus.RESOLVED);
                case "reject" -> close(invocation, args, ReportStatus.REJECTED);
                case "false" -> close(invocation, args, ReportStatus.FALSE_REPORT);
                case "evidence" -> evidence(invocation, args);
                case "note" -> note(invocation, args);
                case "history", "by", "against" -> history(invocation, args);
                case "stats" -> stats(invocation);
                case "export" -> export(invocation, args);
                case "discord" -> discord(invocation, args);
                case "reload" -> {
                    plugin.reloadRuntime();
                    plugin.messages().send(invocation.source(), "&aConfiguracion recargada.");
                }
                default -> help(invocation);
            }
        } catch (ValidationException ex) {
            plugin.messages().send(invocation.source(), "&c" + ex.getMessage());
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 1) {
            return filter(List.of("help", "list", "view", "claim", "unclaim", "assign", "status", "priority", "close",
                    "reject", "false", "evidence", "note", "history", "against", "by", "stats", "export", "discord", "reload"), args[0]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("status")) {
            return Arrays.stream(ReportStatus.values()).map(Enum::name).toList();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("priority")) {
            return Arrays.stream(ReportPriority.values()).map(Enum::name).toList();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("export")) {
            return List.of("json", "html");
        }
        if (args.length == 2 && List.of("history", "against", "by").contains(args[0].toLowerCase())) {
            return filter(plugin.proxyServer().getAllPlayers().stream().map(Player::getUsername).toList(), args[1]);
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("sentinelreports.staff");
    }

    private void list(Invocation invocation, String[] args) {
        ReportStatus status = args.length > 1 ? ReportStatus.parse(args[1]).orElse(null) : null;
        if (args.length > 1 && args[1].equalsIgnoreCase("open")) {
            status = ReportStatus.OPEN;
        }
        var page = plugin.reportService().query(new ReportQuery(new ReportFilter(status, null, null, null, null, null, null, 0L, 0L),
                1, 10, "created_at", true));
        plugin.messages().line(invocation.source());
        plugin.messages().send(invocation.source(), "&b&lSentinelReports &8» &fReportes");
        plugin.messages().line(invocation.source());
        page.items().forEach(report -> plugin.messages().send(invocation.source(), "&b#" + report.id() + " &8| &f" + report.targetName()
                + " &8<- &7" + report.reporterName() + " &8| &e" + report.status() + " &8| &7" + TimeFormatter.since(report.createdAt())));
        plugin.messages().line(invocation.source());
    }

    private void view(Invocation invocation, String[] args) {
        Report report = report(args, 1);
        plugin.messages().line(invocation.source());
        plugin.messages().send(invocation.source(), "&b&lSentinelReports &8» &fReporte #" + report.id());
        plugin.messages().line(invocation.source());
        plugin.messages().send(invocation.source(), "&7Reportado: &f" + report.targetName());
        plugin.messages().send(invocation.source(), "&7Reportador: &f" + report.reporterName());
        plugin.messages().send(invocation.source(), "&7Categoria: &f" + report.category());
        plugin.messages().send(invocation.source(), "&7Prioridad: &f" + report.priority());
        plugin.messages().send(invocation.source(), "&7Estado: &f" + report.status());
        plugin.messages().send(invocation.source(), "&7Servidor: &f" + report.serverName());
        plugin.messages().send(invocation.source(), "&7Razon: &f" + report.reason());
        plugin.messages().line(invocation.source());
    }

    private void claim(Invocation invocation, String[] args) {
        long id = id(args, 1);
        UUID uuid = invocation.source() instanceof Player player ? player.getUniqueId() : new UUID(0L, 0L);
        String name = invocation.source() instanceof Player player ? player.getUsername() : "Console";
        plugin.reportService().claim(id, uuid, name);
        plugin.messages().send(invocation.source(), "&aTomaste el reporte &f#" + id);
    }

    private void unclaim(Invocation invocation, String[] args) {
        long id = id(args, 1);
        UUID uuid = invocation.source() instanceof Player player ? player.getUniqueId() : new UUID(0L, 0L);
        String name = invocation.source() instanceof Player player ? player.getUsername() : "Console";
        plugin.reportService().unclaim(id, uuid, name);
        plugin.messages().send(invocation.source(), "&aLiberaste el reporte &f#" + id);
    }

    private void assign(Invocation invocation, String[] args) {
        long id = id(args, 1);
        Player staff = plugin.proxyServer().getPlayer(args.length > 2 ? args[2] : "").orElseThrow(() -> new ValidationException("errors.player_not_found", "Jugador no encontrado"));
        UUID actorUuid = invocation.source() instanceof Player player ? player.getUniqueId() : new UUID(0L, 0L);
        String actorName = invocation.source() instanceof Player player ? player.getUsername() : "Console";
        plugin.reportService().assign(id, staff.getUniqueId(), staff.getUsername(), actorUuid, actorName);
        plugin.messages().send(invocation.source(), "&aReporte asignado a &f" + staff.getUsername());
    }

    private void status(Invocation invocation, String[] args) {
        long id = id(args, 1);
        ReportStatus status = ReportStatus.parse(args.length > 2 ? args[2] : "").orElseThrow(() -> new ValidationException("errors.invalid_status", "Estado invalido"));
        UUID actorUuid = invocation.source() instanceof Player player ? player.getUniqueId() : new UUID(0L, 0L);
        String actorName = invocation.source() instanceof Player player ? player.getUsername() : "Console";
        plugin.reportService().updateStatus(id, status, actorUuid, actorName);
        plugin.messages().send(invocation.source(), "&aEstado actualizado a &f" + status);
    }

    private void priority(Invocation invocation, String[] args) {
        long id = id(args, 1);
        ReportPriority priority = ReportPriority.parse(args.length > 2 ? args[2] : "").orElseThrow(() -> new ValidationException("errors.invalid_priority", "Prioridad invalida"));
        UUID actorUuid = invocation.source() instanceof Player player ? player.getUniqueId() : new UUID(0L, 0L);
        String actorName = invocation.source() instanceof Player player ? player.getUsername() : "Console";
        plugin.reportService().updatePriority(id, priority, actorUuid, actorName);
        plugin.messages().send(invocation.source(), "&aPrioridad actualizada a &f" + priority);
    }

    private void close(Invocation invocation, String[] args, ReportStatus status) {
        long id = id(args, 1);
        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "";
        UUID actorUuid = invocation.source() instanceof Player player ? player.getUniqueId() : new UUID(0L, 0L);
        String actorName = invocation.source() instanceof Player player ? player.getUsername() : "Console";
        plugin.reportService().closeReport(id, status, actorUuid, actorName, reason);
        plugin.messages().send(invocation.source(), "&aReporte cerrado como &f" + status);
    }

    private void evidence(Invocation invocation, String[] args) {
        if (args.length < 3) {
            plugin.messages().send(invocation.source(), "&cUso: /reports evidence <add|list|remove> <id> [text]");
            return;
        }
        long reportId = id(args, 2);
        UUID actorUuid = invocation.source() instanceof Player player ? player.getUniqueId() : new UUID(0L, 0L);
        String actorName = invocation.source() instanceof Player player ? player.getUsername() : "Console";
        switch (args[1].toLowerCase()) {
            case "add" -> {
                plugin.reportService().addEvidence(reportId, String.join(" ", Arrays.copyOfRange(args, 3, args.length)), actorUuid, actorName);
                plugin.messages().send(invocation.source(), "&aEvidencia anadida.");
            }
            case "list" -> plugin.reportService().evidence(reportId).forEach(e -> plugin.messages().send(invocation.source(), "&b#" + e.id() + " &8| &f" + e.evidenceType() + " &7" + e.content()));
            case "remove" -> {
                long evidenceId = id(args, 3);
                plugin.reportService().removeEvidence(reportId, evidenceId, actorUuid, actorName);
                plugin.messages().send(invocation.source(), "&aEvidencia eliminada.");
            }
            default -> plugin.messages().send(invocation.source(), "&cAccion invalida.");
        }
    }

    private void note(Invocation invocation, String[] args) {
        if (args.length < 3) {
            plugin.messages().send(invocation.source(), "&cUso: /reports note <add|list> <id> [note]");
            return;
        }
        long reportId = id(args, 2);
        UUID actorUuid = invocation.source() instanceof Player player ? player.getUniqueId() : new UUID(0L, 0L);
        String actorName = invocation.source() instanceof Player player ? player.getUsername() : "Console";
        if (args[1].equalsIgnoreCase("add")) {
            plugin.reportService().addNote(reportId, String.join(" ", Arrays.copyOfRange(args, 3, args.length)), actorUuid, actorName);
            plugin.messages().send(invocation.source(), "&aNota anadida.");
        } else {
            plugin.reportService().notes(reportId).forEach(note -> plugin.messages().send(invocation.source(), "&b#" + note.id() + " &8| &f" + note.addedByName() + " &7" + note.note()));
        }
    }

    private void history(Invocation invocation, String[] args) {
        Player player = plugin.proxyServer().getPlayer(args.length > 1 ? args[1] : "").orElseThrow(() -> new ValidationException("errors.player_not_found", "Jugador no encontrado"));
        List<Report> reports = switch (args[0].toLowerCase()) {
            case "by" -> plugin.reportService().getReportsByPlayer(player.getUniqueId());
            case "against" -> plugin.reportService().getReportsAgainst(player.getUniqueId());
            default -> {
                java.util.ArrayList<Report> all = new java.util.ArrayList<>(plugin.reportService().getReportsByPlayer(player.getUniqueId()));
                all.addAll(plugin.reportService().getReportsAgainst(player.getUniqueId()));
                yield all;
            }
        };
        reports.stream().limit(20).forEach(report -> plugin.messages().send(invocation.source(), "&b#" + report.id() + " &8| &7" + report.reporterName() + " &8-> &f" + report.targetName() + " &8| &e" + report.status()));
    }

    private void stats(Invocation invocation) {
        Map<ReportStatus, Long> counts = plugin.reportService().stats().statusCounts();
        plugin.messages().line(invocation.source());
        counts.forEach((status, count) -> plugin.messages().send(invocation.source(), "&7" + status + ": &f" + count));
        plugin.messages().line(invocation.source());
    }

    private void export(Invocation invocation, String[] args) {
        long id = id(args, 1);
        String format = args.length > 2 ? args[2] : "json";
        var path = plugin.reportService().export(id, format);
        plugin.messages().send(invocation.source(), "&aReporte exportado: &f" + path);
    }

    private void discord(Invocation invocation, String[] args) {
        if (args.length > 1 && args[1].equalsIgnoreCase("test")) {
            plugin.reportService().discord().sendTest();
            plugin.messages().send(invocation.source(), "&aTest enviado.");
        }
    }

    private Report report(String[] args, int index) {
        return plugin.reportService().getReportById(id(args, index)).orElseThrow(() -> new ValidationException("errors.invalid_report", "Reporte no encontrado"));
    }

    private long id(String[] args, int index) {
        if (index >= args.length) {
            throw new ValidationException("errors.invalid_report", "ID invalido");
        }
        return CommandArgumentParser.positiveLong(args[index]).orElseThrow(() -> new ValidationException("errors.invalid_report", "ID invalido"));
    }

    private void help(Invocation invocation) {
        plugin.messages().line(invocation.source());
        plugin.messages().send(invocation.source(), "&b&lSentinelReports &8» &fAyuda Staff");
        plugin.messages().line(invocation.source());
        plugin.messages().send(invocation.source(), "&b/reports list open &7Lista reportes abiertos.");
        plugin.messages().send(invocation.source(), "&b/reports view <id> &7Ver reporte.");
        plugin.messages().send(invocation.source(), "&b/reports claim <id> &7Tomar reporte.");
        plugin.messages().send(invocation.source(), "&b/reports close <id> <reason> &7Cerrar reporte.");
        plugin.messages().send(invocation.source(), "&b/reports evidence add <id> <text/link> &7Anadir evidencia.");
        plugin.messages().line(invocation.source());
    }

    private List<String> filter(List<String> values, String prefix) {
        String lower = prefix == null ? "" : prefix.toLowerCase();
        return values.stream().filter(value -> value.toLowerCase().startsWith(lower)).toList();
    }
}
