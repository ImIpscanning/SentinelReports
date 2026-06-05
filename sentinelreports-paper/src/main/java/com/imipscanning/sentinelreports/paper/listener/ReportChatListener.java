package com.imipscanning.sentinelreports.paper.listener;

import com.imipscanning.sentinelreports.paper.SentinelReportsPaperPlugin;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class ReportChatListener implements Listener {
    private final SentinelReportsPaperPlugin plugin;

    public ReportChatListener(SentinelReportsPaperPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (plugin.sessionManager().get(player.getUniqueId()).isEmpty()) {
            return;
        }
        event.setCancelled(true);
        String message = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();
        if (message.equalsIgnoreCase("cancel")) {
            plugin.scheduler().runGlobal(() -> plugin.sessionManager().cancel(player));
            return;
        }
        plugin.sessionManager().complete(player, message);
    }
}
