package com.imipscanning.sentinelreports.paper.platform;

import com.imipscanning.sentinelreports.common.util.JsonUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.nio.charset.StandardCharsets;
import java.util.List;

public final class PaperPluginMessenger {
    public static final String CHANNEL = "sentinelreports:sync";
    private final Plugin plugin;
    private final boolean enabled;

    public PaperPluginMessenger(Plugin plugin, boolean enabled) {
        this.plugin = plugin;
        this.enabled = enabled;
    }

    public void register() {
        if (!enabled) {
            return;
        }
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL);
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, CHANNEL, (channel, player, message) -> {
            // Velocity can mirror staff alerts here; Paper currently receives only lightweight sync notices.
        });
    }

    public void unregister() {
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin, CHANNEL);
        Bukkit.getMessenger().unregisterIncomingPluginChannel(plugin, CHANNEL);
    }

    public void broadcastNewReport(List<String> lines) {
        if (!enabled) {
            return;
        }
        Player carrier = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
        if (carrier == null) {
            return;
        }
        String payload = JsonUtil.toJson(new SyncMessage("NEW_REPORT", lines));
        carrier.sendPluginMessage(plugin, CHANNEL, payload.getBytes(StandardCharsets.UTF_8));
    }

    public record SyncMessage(String type, List<String> lines) {
    }
}
