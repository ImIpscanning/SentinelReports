package com.imipscanning.sentinelreports.paper.gui;

import com.imipscanning.sentinelreports.common.model.Report;
import com.imipscanning.sentinelreports.common.model.ReportCategory;
import com.imipscanning.sentinelreports.common.model.ReportFilter;
import com.imipscanning.sentinelreports.common.model.ReportQuery;
import com.imipscanning.sentinelreports.common.model.ReportStatus;
import com.imipscanning.sentinelreports.common.util.ColorFormatter;
import com.imipscanning.sentinelreports.common.util.PlaceholderFormatter;
import com.imipscanning.sentinelreports.common.util.TimeFormatter;
import com.imipscanning.sentinelreports.paper.SentinelReportsPaperPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ReportGuiManager {
    private final SentinelReportsPaperPlugin plugin;
    private YamlConfiguration gui;
    private NamespacedKey actionKey;
    private NamespacedKey categoryKey;
    private NamespacedKey reportIdKey;

    public ReportGuiManager(SentinelReportsPaperPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "gui.yml");
        if (!file.exists()) {
            plugin.saveResource("gui.yml", false);
        }
        this.gui = YamlConfiguration.loadConfiguration(file);
        this.actionKey = new NamespacedKey(plugin, "action");
        this.categoryKey = new NamespacedKey(plugin, "category");
        this.reportIdKey = new NamespacedKey(plugin, "report_id");
    }

    public void openPlayerMain(Player player) {
        ReportInventoryHolder holder = new ReportInventoryHolder(ReportInventoryHolder.Type.PLAYER_MAIN, null, null, 0L);
        Inventory inventory = create(holder, "main_player_menu");
        fill(inventory, "main_player_menu.filler");
        ConfigurationSection section = gui.getConfigurationSection("main_player_menu.items");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                ItemStack item = item("main_player_menu.items." + key, Map.of(), key, null, 0L);
                inventory.setItem(gui.getInt("main_player_menu.items." + key + ".slot", 0), item);
            }
        }
        player.openInventory(inventory);
    }

    public void openCategory(Player player, Player target) {
        ReportInventoryHolder holder = new ReportInventoryHolder(ReportInventoryHolder.Type.CATEGORY, target.getUniqueId(), target.getName(), 0L);
        Inventory inventory = create(holder, "category_menu", Map.of("target", target.getName()));
        fill(inventory, "category_menu.filler");
        ConfigurationSection categories = gui.getConfigurationSection("category_menu.categories");
        if (categories != null) {
            for (String key : categories.getKeys(false)) {
                if (key.equals("back") || key.equals("cancel")) {
                    ItemStack item = item("category_menu.categories." + key, Map.of("target", target.getName()), key, null, 0L);
                    inventory.setItem(gui.getInt("category_menu.categories." + key + ".slot", 0), item);
                    continue;
                }
                ReportCategory category = plugin.reportService().categories().stream()
                        .filter(candidate -> candidate.id().equals(key))
                        .findFirst()
                        .orElse(null);
                if (category == null) {
                    continue;
                }
                ItemStack item = item("category_menu.categories." + key, Map.of("target", target.getName()), "category", key, 0L);
                inventory.setItem(gui.getInt("category_menu.categories." + key + ".slot", 0), item);
            }
        }
        player.openInventory(inventory);
    }

    public void openStaff(Player player) {
        ReportInventoryHolder holder = new ReportInventoryHolder(ReportInventoryHolder.Type.STAFF_MAIN, null, null, 0L);
        Inventory inventory = create(holder, "staff_menu");
        fill(inventory, "staff_menu.filler");
        ConfigurationSection section = gui.getConfigurationSection("staff_menu.items");
        boolean showOnlyAllowed = plugin.getConfig().getBoolean("hub.show_only_allowed_buttons", true);
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String permission = gui.getString("staff_menu.items." + key + ".permission");
                if (showOnlyAllowed && permission != null && !permission.isBlank() && !player.hasPermission(permission)) {
                    continue;
                }
                ItemStack item = item("staff_menu.items." + key, Map.of(), key, null, 0L);
                inventory.setItem(gui.getInt("staff_menu.items." + key + ".slot", 0), item);
            }
        }
        player.openInventory(inventory);
    }

    public void openReports(Player player, ReportStatus status) {
        plugin.scheduler().runAsync(() -> {
            var page = plugin.reportService().query(new ReportQuery(
                    new ReportFilter(status, null, null, null, null, null, null, 0L, 0L),
                    1, 45, "created_at", true));
            plugin.scheduler().runGlobal(() -> {
                ReportInventoryHolder holder = new ReportInventoryHolder(ReportInventoryHolder.Type.OPEN_REPORTS, null, null, 0L);
                Inventory inventory = create(holder, "open_reports_menu");
                int slot = 0;
                for (Report report : page.items()) {
                    inventory.setItem(slot++, reportItem(report));
                }
                player.openInventory(inventory);
            });
        });
    }

    public void openDetail(Player player, long reportId) {
        plugin.scheduler().runAsync(() -> {
            var report = plugin.reportService().getReportById(reportId).orElse(null);
            if (report == null) {
                plugin.scheduler().runGlobal(() -> plugin.messages().send(player, "errors.invalid_report", Map.of(), "&cEse reporte no existe."));
                return;
            }
            var evidence = plugin.reportService().evidence(reportId);
            var notes = plugin.reportService().notes(reportId);
            plugin.scheduler().runGlobal(() -> {
                ReportInventoryHolder holder = new ReportInventoryHolder(ReportInventoryHolder.Type.REPORT_DETAIL, null, null, reportId);
                Inventory inventory = create(holder, "report_detail_menu", Map.of("id", report.id()));
                inventory.setItem(10, named(Material.PLAYER_HEAD, "&c" + report.targetName(), List.of("&7Reportado", "&7UUID: &f" + report.targetUuid()), "none", null, report.id()));
                inventory.setItem(12, named(Material.PAPER, "&fRazon", List.of("&7" + report.reason()), "none", null, report.id()));
                inventory.setItem(14, named(Material.BOOK, "&bEvidencias", List.of("&7Total: &f" + evidence.size(), "&8/reports evidence list " + report.id()), "evidence", null, report.id()));
                inventory.setItem(16, named(Material.NAME_TAG, "&eNotas", List.of("&7Total: &f" + notes.size(), "&8/reports note list " + report.id()), "notes", null, report.id()));
                inventory.setItem(28, named(Material.COMPASS, "&aTeleport", List.of("&7Click para teletransportarte."), "teleport", null, report.id()));
                inventory.setItem(30, named(Material.EMERALD, "&aClaim", List.of("&7Tomar reporte."), "claim", null, report.id()));
                inventory.setItem(32, named(Material.REDSTONE, "&cCritico", List.of("&7Cambiar prioridad a CRITICAL."), "critical", null, report.id()));
                inventory.setItem(34, named(Material.LIME_DYE, "&aResolver", List.of("&7Cerrar como resuelto."), "resolve", null, report.id()));
                inventory.setItem(44, named(Material.BARRIER, "&cCerrar", List.of(), "close", null, report.id()));
                player.openInventory(inventory);
            });
        });
    }

    public NamespacedKey actionKey() {
        return actionKey;
    }

    public NamespacedKey categoryKey() {
        return categoryKey;
    }

    public NamespacedKey reportIdKey() {
        return reportIdKey;
    }

    private Inventory create(ReportInventoryHolder holder, String path) {
        return create(holder, path, Map.of());
    }

    private Inventory create(ReportInventoryHolder holder, String path, Map<String, ?> placeholders) {
        int size = gui.getInt(path + ".size", 27);
        Component title = ColorFormatter.component(PlaceholderFormatter.format(gui.getString(path + ".title", "&8SentinelReports"), placeholders));
        Inventory inventory = Bukkit.createInventory(holder, size, title);
        holder.inventory(inventory);
        return inventory;
    }

    private void fill(Inventory inventory, String path) {
        if (!gui.getBoolean(path + ".enabled", false)) {
            return;
        }
        ItemStack filler = named(material(gui.getString(path + ".material", "GRAY_STAINED_GLASS_PANE")), gui.getString(path + ".name", " "), List.of(), "none", null, 0L);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }
    }

    private ItemStack item(String path, Map<String, ?> placeholders, String action, String category, long reportId) {
        Material material = material(gui.getString(path + ".material", "PAPER"));
        String name = PlaceholderFormatter.format(gui.getString(path + ".name", "&fItem"), placeholders);
        List<String> lore = gui.getStringList(path + ".lore").stream()
                .map(line -> PlaceholderFormatter.format(line, placeholders))
                .toList();
        return named(material, name, lore, action, category, reportId);
    }

    private ItemStack reportItem(Report report) {
        String configured = gui.getString("open_reports_menu.priority_materials." + report.priority().name(), "PAPER");
        List<String> lore = new ArrayList<>();
        lore.add("&7ID: &f#" + report.id());
        lore.add("&7Reportado: &f" + report.targetName());
        lore.add("&7Reportador: &f" + report.reporterName());
        lore.add("&7Categoria: &f" + report.category());
        lore.add("&7Prioridad: &f" + report.priority());
        lore.add("&7Servidor: &f" + report.serverName());
        lore.add("&7Tiempo: &f" + TimeFormatter.since(report.createdAt()));
        lore.add("&7Estado: &f" + report.status());
        lore.add("");
        lore.add("&eClick izquierdo: &fVer");
        lore.add("&eClick derecho: &fClaim");
        lore.add("&eShift click: &fCerrar");
        return named(material(configured), "&bReporte #" + report.id(), lore, "report", null, report.id());
    }

    private ItemStack named(Material material, String name, List<String> lore, String action, String category, long reportId) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(ColorFormatter.component(name));
        meta.lore(lore.stream().map(ColorFormatter::component).toList());
        meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action == null ? "none" : action);
        if (category != null) {
            meta.getPersistentDataContainer().set(categoryKey, PersistentDataType.STRING, category);
        }
        if (reportId > 0) {
            meta.getPersistentDataContainer().set(reportIdKey, PersistentDataType.LONG, reportId);
        }
        stack.setItemMeta(meta);
        return stack;
    }

    private Material material(String value) {
        if (value == null) {
            return Material.PAPER;
        }
        Material material = Material.matchMaterial(value);
        return material == null ? Material.PAPER : material;
    }
}
