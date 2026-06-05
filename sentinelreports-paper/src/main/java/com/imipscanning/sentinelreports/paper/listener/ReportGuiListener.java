package com.imipscanning.sentinelreports.paper.listener;

import com.imipscanning.sentinelreports.common.model.ReportStatus;
import com.imipscanning.sentinelreports.paper.SentinelReportsPaperPlugin;
import com.imipscanning.sentinelreports.paper.gui.ReportInventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public final class ReportGuiListener implements Listener {
    private final SentinelReportsPaperPlugin plugin;

    public ReportGuiListener(SentinelReportsPaperPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!(event.getInventory().getHolder() instanceof ReportInventoryHolder holder)) {
            return;
        }
        event.setCancelled(true);
        ItemStack stack = event.getCurrentItem();
        if (stack == null || !stack.hasItemMeta()) {
            return;
        }
        PersistentDataContainer data = stack.getItemMeta().getPersistentDataContainer();
        String action = data.getOrDefault(plugin.guiManager().actionKey(), PersistentDataType.STRING, "none");
        long reportId = data.getOrDefault(plugin.guiManager().reportIdKey(), PersistentDataType.LONG, holder.reportId());
        switch (holder.type()) {
            case PLAYER_MAIN -> handlePlayerMain(player, action);
            case CATEGORY -> handleCategory(player, holder, data, action);
            case STAFF_MAIN -> handleStaffMain(player, action);
            case OPEN_REPORTS -> handleOpenReports(player, event.getClick(), action, reportId);
            case REPORT_DETAIL -> handleDetail(player, action, reportId);
        }
    }

    private void handlePlayerMain(Player player, String action) {
        switch (action) {
            case "my_reports" -> player.performCommand("report status");
            case "info" -> player.performCommand("report help");
            case "close" -> player.closeInventory();
            case "report_player" -> {
                player.closeInventory();
                plugin.messages().send(player, "report.select_player", Map.of(), "&eUsa &f/report <player> &epara elegir jugador.");
            }
            default -> {
            }
        }
    }

    private void handleCategory(Player player, ReportInventoryHolder holder, PersistentDataContainer data, String action) {
        if (action.equals("cancel")) {
            player.closeInventory();
            return;
        }
        if (action.equals("back")) {
            plugin.guiManager().openPlayerMain(player);
            return;
        }
        String category = data.get(plugin.guiManager().categoryKey(), PersistentDataType.STRING);
        if (category == null) {
            return;
        }
        Player target = Bukkit.getPlayer(holder.targetUuid());
        if (target == null) {
            plugin.messages().send(player, "errors.player_not_found", Map.of(), "&cJugador no encontrado.");
            return;
        }
        player.closeInventory();
        if (plugin.reportSettings().requireReason()) {
            plugin.sessionManager().start(player, target, category);
        } else {
            plugin.sessionManager().start(player, target, category);
            plugin.sessionManager().complete(player, "Motivo predefinido: " + category);
        }
    }

    private void handleStaffMain(Player player, String action) {
        switch (action) {
            case "open_reports" -> plugin.guiManager().openReports(player, ReportStatus.OPEN);
            case "assigned_reports" -> player.performCommand("reports list assigned");
            case "all_reports" -> player.performCommand("reports list");
            case "history" -> player.performCommand("reports help history");
            case "stats" -> player.performCommand("reports stats");
            case "discord" -> player.performCommand("reports discord test");
            case "reload" -> player.performCommand("reports reload");
            case "close" -> player.closeInventory();
            default -> {
            }
        }
    }

    private void handleOpenReports(Player player, ClickType click, String action, long reportId) {
        if (!action.equals("report") || reportId <= 0) {
            return;
        }
        if (click.isRightClick()) {
            player.performCommand("reports claim " + reportId);
        } else if (click.isShiftClick()) {
            player.performCommand("reports close " + reportId + " Resuelto desde GUI");
        } else {
            plugin.guiManager().openDetail(player, reportId);
        }
    }

    private void handleDetail(Player player, String action, long reportId) {
        switch (action) {
            case "evidence" -> player.performCommand("reports evidence list " + reportId);
            case "notes" -> player.performCommand("reports note list " + reportId);
            case "teleport" -> player.performCommand("reports teleport " + reportId);
            case "claim" -> player.performCommand("reports claim " + reportId);
            case "critical" -> player.performCommand("reports priority " + reportId + " CRITICAL");
            case "resolve" -> player.performCommand("reports close " + reportId + " Resuelto desde GUI");
            case "close" -> player.closeInventory();
            default -> {
            }
        }
    }
}
