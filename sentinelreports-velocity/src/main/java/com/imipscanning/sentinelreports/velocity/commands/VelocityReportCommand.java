package com.imipscanning.sentinelreports.velocity.commands;

import com.imipscanning.sentinelreports.common.model.ReportCreateRequest;
import com.imipscanning.sentinelreports.common.service.ValidationException;
import com.imipscanning.sentinelreports.common.util.TimeFormatter;
import com.imipscanning.sentinelreports.velocity.SentinelReportsVelocityPlugin;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import java.time.Duration;
import java.util.List;

public final class VelocityReportCommand implements SimpleCommand {
    private final SentinelReportsVelocityPlugin plugin;

    public VelocityReportCommand(SentinelReportsVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player reporter)) {
            plugin.messages().send(invocation.source(), "&cSolo jugadores pueden crear reportes.");
            return;
        }
        String[] args = invocation.arguments();
        if (args.length < 3 || args[0].equalsIgnoreCase("help")) {
            help(invocation);
            return;
        }
        Player target = plugin.proxyServer().getPlayer(args[0]).orElse(null);
        if (target == null) {
            plugin.messages().send(reporter, "&cJugador no encontrado.");
            return;
        }
        String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
        String server = target.getCurrentServer().map(connection -> connection.getServerInfo().getName()).orElse(plugin.reportSettings().serverName());
        ReportCreateRequest request = new ReportCreateRequest(
                reporter.getUniqueId(), reporter.getUsername(),
                target.getUniqueId(), target.getUsername(),
                args[1], reason, server, null, null, null, null,
                target.hasPermission("sentinelreports.staff"),
                reporter.hasPermission("sentinelreports.bypass.cooldown"),
                reporter.hasPermission("sentinelreports.bypass.limit"));
        try {
            var report = plugin.reportService().createReport(request);
            plugin.messages().line(reporter);
            plugin.messages().send(reporter, "&b&lSentinelReports &8» &aReporte creado");
            plugin.messages().send(reporter, "&7ID: &f#" + report.id());
            plugin.messages().send(reporter, "&7Reportado: &f" + report.targetName());
            plugin.messages().send(reporter, "&7Categoria: &f" + report.category());
            plugin.messages().send(reporter, "&7Prioridad: &f" + report.priority());
            plugin.messages().line(reporter);
        } catch (ValidationException ex) {
            String message = ex.messageKey().equals("errors.cooldown")
                    ? "&cDebes esperar &f" + remaining(ex) + " &cantes de reportar otra vez."
                    : "&cNo se pudo crear el reporte: &f" + ex.getMessage();
            plugin.messages().send(reporter, message);
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 1) {
            return plugin.proxyServer().getAllPlayers().stream().map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2) {
            return plugin.reportService().categories().stream().map(com.imipscanning.sentinelreports.common.model.ReportCategory::id)
                    .filter(id -> id.startsWith(args[1].toLowerCase())).toList();
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("sentinelreports.report");
    }

    private void help(Invocation invocation) {
        plugin.messages().line(invocation.source());
        plugin.messages().send(invocation.source(), "&b&lSentinelReports &8» &fAyuda");
        plugin.messages().line(invocation.source());
        plugin.messages().send(invocation.source(), "&b/report <player> <category> <reason>");
        plugin.messages().send(invocation.source(), "&7Ejemplo: &f/report Steve cheating Kill aura");
        plugin.messages().line(invocation.source());
    }

    private String remaining(ValidationException ex) {
        try {
            return TimeFormatter.compact(Duration.ofMillis(Long.parseLong(ex.getMessage())));
        } catch (NumberFormatException ignored) {
            return "unos segundos";
        }
    }
}
