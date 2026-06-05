package com.imipscanning.sentinelreports.paper.platform;

import com.imipscanning.sentinelreports.common.service.PlaceholderService;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public final class SentinelPlaceholderExpansion extends PlaceholderExpansion {
    private final PlaceholderService placeholderService;

    public SentinelPlaceholderExpansion(PlaceholderService placeholderService) {
        this.placeholderService = placeholderService;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "sentinelreports";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ImIpscanning";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase("open_reports")) {
            return placeholderService.openReports();
        }
        if (player == null || player.getUniqueId() == null) {
            return "0";
        }
        return switch (params.toLowerCase()) {
            case "assigned_reports" -> placeholderService.assignedReports(player.getUniqueId());
            case "player_reports" -> placeholderService.playerReports(player.getUniqueId());
            case "player_false_reports" -> placeholderService.playerFalseReports(player.getUniqueId());
            default -> null;
        };
    }
}
