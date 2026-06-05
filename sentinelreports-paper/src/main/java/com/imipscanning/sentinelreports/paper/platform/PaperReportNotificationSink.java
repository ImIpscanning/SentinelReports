package com.imipscanning.sentinelreports.paper.platform;

import com.imipscanning.sentinelreports.common.service.ReportNotificationService;
import com.imipscanning.sentinelreports.common.util.ColorFormatter;
import com.imipscanning.sentinelreports.paper.SentinelReportsPaperPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public final class PaperReportNotificationSink implements ReportNotificationService.Sink {
    private final SentinelReportsPaperPlugin plugin;

    public PaperReportNotificationSink(SentinelReportsPaperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void notifyStaff(List<String> lines) {
        plugin.scheduler().runGlobal(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission("sentinelreports.staff")) {
                    continue;
                }
                lines.forEach(line -> player.sendMessage(ColorFormatter.component(line)));
            }
            plugin.pluginMessenger().broadcastNewReport(lines);
        });
    }
}
