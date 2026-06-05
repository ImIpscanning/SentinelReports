package com.imipscanning.sentinelreports.velocity.platform;

import com.imipscanning.sentinelreports.common.service.ReportNotificationService;
import com.imipscanning.sentinelreports.common.util.ColorFormatter;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.List;

public final class VelocityNotificationSink implements ReportNotificationService.Sink {
    private final ProxyServer proxyServer;

    public VelocityNotificationSink(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Override
    public void notifyStaff(List<String> lines) {
        proxyServer.getAllPlayers().stream()
                .filter(player -> player.hasPermission("sentinelreports.staff"))
                .forEach(player -> lines.forEach(line -> player.sendMessage(ColorFormatter.component(line))));
    }
}
