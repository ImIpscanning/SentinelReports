package com.imipscanning.sentinelreports.paper.session;

import com.imipscanning.sentinelreports.common.model.ReportCreateRequest;
import com.imipscanning.sentinelreports.common.model.ReportSession;
import com.imipscanning.sentinelreports.common.service.ReportService;
import com.imipscanning.sentinelreports.common.service.ValidationException;
import com.imipscanning.sentinelreports.common.util.TimeFormatter;
import com.imipscanning.sentinelreports.paper.SentinelReportsPaperPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ReportSessionManager {
    private final SentinelReportsPaperPlugin plugin;
    private final Map<UUID, ReportSession> sessions = new ConcurrentHashMap<>();

    public ReportSessionManager(SentinelReportsPaperPlugin plugin) {
        this.plugin = plugin;
    }

    public void start(Player reporter, Player target, String category) {
        long timeoutMillis = plugin.reportSettings().reasonTimeoutSeconds() * 1000L;
        ReportSession session = new ReportSession(reporter.getUniqueId(), target.getUniqueId(), target.getName(), category,
                System.currentTimeMillis() + timeoutMillis);
        sessions.put(reporter.getUniqueId(), session);
        plugin.messages().sendList(reporter, "report.reason_prompt", Map.of(
                "target", target.getName(),
                "seconds", plugin.reportSettings().reasonTimeoutSeconds()
        ), java.util.List.of(
                "&8&m------------------------------------",
                "&7Escribe la razon del reporte contra &f%target%&7.",
                "&7Escribe &ccancel &7para cancelar.",
                "&8&m------------------------------------"
        ));
        plugin.scheduler().runLater(() -> expire(reporter.getUniqueId(), session), plugin.reportSettings().reasonTimeoutSeconds() * 20L);
    }

    public Optional<ReportSession> get(UUID reporterUuid) {
        ReportSession session = sessions.get(reporterUuid);
        if (session == null) {
            return Optional.empty();
        }
        if (session.expired(System.currentTimeMillis())) {
            sessions.remove(reporterUuid);
            return Optional.empty();
        }
        return Optional.of(session);
    }

    public void cancel(Player player) {
        sessions.remove(player.getUniqueId());
        plugin.messages().send(player, "success.report_cancelled", Map.of(), "&cReporte cancelado.");
    }

    public void complete(Player reporter, String reason) {
        ReportSession session = sessions.remove(reporter.getUniqueId());
        if (session == null) {
            return;
        }
        Player target = Bukkit.getPlayer(session.targetUuid());
        if (target == null) {
            plugin.messages().send(reporter, "errors.player_not_found", Map.of(), "&cJugador no encontrado.");
            return;
        }
        Location location = target.getLocation();
        ReportCreateRequest request = new ReportCreateRequest(
                reporter.getUniqueId(),
                reporter.getName(),
                target.getUniqueId(),
                target.getName(),
                session.category(),
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
        plugin.scheduler().runAsync(() -> {
            try {
                var report = plugin.reportService().createReport(request);
                plugin.scheduler().runGlobal(() -> plugin.messages().sendList(reporter, "report.created_panel", Map.of(
                        "id", report.id(),
                        "target", report.targetName(),
                        "category", report.category(),
                        "priority", report.priority(),
                        "reason", report.reason()
                ), java.util.List.of("&aReporte creado. ID: &f#" + report.id())));
            } catch (ValidationException ex) {
                plugin.scheduler().runGlobal(() -> plugin.messages().send(reporter, ex.messageKey(), Map.of(
                        "time", TimeFormatter.compact(Duration.ofMillis(parseRemaining(ex)))
                ), "&cNo se pudo crear el reporte: " + ex.getMessage()));
            }
        });
    }

    private void expire(UUID reporterUuid, ReportSession expected) {
        ReportSession current = sessions.get(reporterUuid);
        if (current == expected && current.expired(System.currentTimeMillis())) {
            sessions.remove(reporterUuid);
            Player player = Bukkit.getPlayer(reporterUuid);
            if (player != null) {
                plugin.messages().send(player, "errors.reason_timeout", Map.of(), "&cEl reporte expiro.");
            }
        }
    }

    private long parseRemaining(ValidationException ex) {
        try {
            return Long.parseLong(ex.getMessage());
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }
}
