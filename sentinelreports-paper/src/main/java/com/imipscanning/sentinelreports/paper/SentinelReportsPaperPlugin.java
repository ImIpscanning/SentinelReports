package com.imipscanning.sentinelreports.paper;

import com.imipscanning.sentinelreports.common.api.SentinelReportsProvider;
import com.imipscanning.sentinelreports.common.model.ReportCategory;
import com.imipscanning.sentinelreports.common.model.ReportPriority;
import com.imipscanning.sentinelreports.common.model.ReportStatus;
import com.imipscanning.sentinelreports.common.service.*;
import com.imipscanning.sentinelreports.common.storage.*;
import com.imipscanning.sentinelreports.common.util.TimeParser;
import com.imipscanning.sentinelreports.paper.commands.ReportCommand;
import com.imipscanning.sentinelreports.paper.commands.ReportsCommand;
import com.imipscanning.sentinelreports.paper.gui.ReportGuiManager;
import com.imipscanning.sentinelreports.paper.listener.ReportChatListener;
import com.imipscanning.sentinelreports.paper.listener.ReportGuiListener;
import com.imipscanning.sentinelreports.paper.platform.*;
import com.imipscanning.sentinelreports.paper.session.ReportSessionManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public final class SentinelReportsPaperPlugin extends JavaPlugin {
    private DatabaseManager databaseManager;
    private ReportRepository reportRepository;
    private EvidenceRepository evidenceRepository;
    private NoteRepository noteRepository;
    private CategoryRepository categoryRepository;
    private ActionRepository actionRepository;
    private ReportService reportService;
    private ReportSettings reportSettings;
    private PaperMessages messages;
    private PaperSchedulerAdapter scheduler;
    private ReportGuiManager guiManager;
    private ReportSessionManager sessionManager;
    private ReportHubService hubService;
    private PaperPluginMessenger pluginMessenger;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResourceIfMissing("messages.yml");
        saveResourceIfMissing("gui.yml");

        messages = new PaperMessages(this);
        scheduler = new PaperSchedulerAdapter(this);
        pluginMessenger = new PaperPluginMessenger(this, getConfig().getBoolean("settings.sync_with_velocity", true));
        pluginMessenger.register();

        startStorage();
        buildServices();

        guiManager = new ReportGuiManager(this);
        sessionManager = new ReportSessionManager(this);
        hubService = new ReportHubService();

        registerCommands();
        getServer().getPluginManager().registerEvents(new ReportChatListener(this), this);
        getServer().getPluginManager().registerEvents(new ReportGuiListener(this), this);
        registerPlaceholderApi();

        SentinelReportsProvider.register(reportService);
        getLogger().info("SentinelReports Paper enabled.");
    }

    @Override
    public void onDisable() {
        SentinelReportsProvider.unregister(reportService);
        if (pluginMessenger != null) {
            pluginMessenger.unregister();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    public void reloadRuntime() {
        reloadConfig();
        messages.reload();
        guiManager.reload();
        categoryRepository.upsertAll(loadCategories());
    }

    private void startStorage() {
        databaseManager = new DatabaseManager(databaseSettings());
        databaseManager.start();
        new MigrationManager(databaseManager).migrate();
        reportRepository = new ReportRepository(databaseManager);
        evidenceRepository = new EvidenceRepository(databaseManager);
        noteRepository = new NoteRepository(databaseManager);
        categoryRepository = new CategoryRepository(databaseManager);
        actionRepository = new ActionRepository(databaseManager);
        categoryRepository.upsertAll(loadCategories());
    }

    private void buildServices() {
        reportSettings = loadReportSettings();
        AbuseProtectionSettings abuseSettings = abuseSettings();
        CooldownService cooldownService = new CooldownService();
        AbuseDetectionService abuseDetectionService = new AbuseDetectionService(abuseSettings, cooldownService, reportRepository);
        ReportValidationService validationService = new ReportValidationService(reportSettings, categoryRepository, abuseDetectionService);
        Executor executor = command -> scheduler.runAsync(command);
        DiscordWebhookService discord = new DiscordWebhookService(discordSettings(), executor);
        ReportNotificationService notification = new ReportNotificationService(new PaperReportNotificationSink(this),
                messages.rawList("report.created_staff_alert", Map.of(), List.of("&cNuevo reporte &f#%id%")));
        ReportCreationService creation = new ReportCreationService(reportSettings, validationService, abuseDetectionService,
                reportRepository, actionRepository, notification, discord);
        ReportAssignmentService assignment = new ReportAssignmentService(reportRepository, actionRepository, discord);
        ReportSearchService search = new ReportSearchService(reportRepository);
        ReportHistoryService history = new ReportHistoryService(reportRepository);
        PlayerStatsRepository playerStatsRepository = new PlayerStatsRepository(reportRepository);
        ReportStatsService stats = new ReportStatsService(reportRepository, playerStatsRepository, actionRepository);
        ReportExportService export = new ReportExportService(reportSettings, reportRepository, evidenceRepository, noteRepository, actionRepository);
        reportService = new ReportService(creation, assignment, validationService, search, history, stats, export,
                reportRepository, evidenceRepository, noteRepository, categoryRepository, actionRepository, discord);
    }

    private void registerCommands() {
        ReportCommand reportCommand = new ReportCommand(this);
        ReportsCommand reportsCommand = new ReportsCommand(this);
        var reportPluginCommand = getCommand("report");
        if (reportPluginCommand != null) {
            reportPluginCommand.setExecutor(reportCommand);
            reportPluginCommand.setTabCompleter(reportCommand);
        }
        var reportsPluginCommand = getCommand("reports");
        if (reportsPluginCommand != null) {
            reportsPluginCommand.setExecutor(reportsCommand);
            reportsPluginCommand.setTabCompleter(reportsCommand);
        }
    }

    private void registerPlaceholderApi() {
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new SentinelPlaceholderExpansion(new PlaceholderService(reportRepository)).register();
        }
    }

    private DatabaseSettings databaseSettings() {
        String type = getConfig().getString("database.type", "sqlite");
        DatabaseType databaseType = DatabaseType.parse(type);
        Path sqlite = getDataFolder().toPath().resolve(getConfig().getString("database.sqlite_file", "sentinelreports.db"));
        return new DatabaseSettings(databaseType, sqlite,
                getConfig().getString("database.mysql.host", "localhost"),
                getConfig().getInt("database.mysql.port", 3306),
                getConfig().getString("database.mysql.database", "sentinelreports"),
                getConfig().getString("database.mysql.username", "root"),
                getConfig().getString("database.mysql.password", "change-me"),
                getConfig().getInt("database.mysql.pool_size", 10));
    }

    private ReportSettings loadReportSettings() {
        Path exportFolder = getDataFolder().toPath().resolve(getConfig().getString("reports.export_folder", "exports"));
        return new ReportSettings(
                getConfig().getBoolean("settings.require_reason", true),
                getConfig().getInt("settings.reason_timeout_seconds", 60),
                ReportStatus.parse(getConfig().getString("reports.default_status", "OPEN")).orElse(ReportStatus.OPEN),
                ReportPriority.parse(getConfig().getString("reports.default_priority", "MEDIUM")).orElse(ReportPriority.MEDIUM),
                getConfig().getBoolean("reports.close_requires_reason", true),
                exportFolder,
                getConfig().getString("settings.server_name", "survival"));
    }

    private AbuseProtectionSettings abuseSettings() {
        boolean allowSelf = getConfig().getBoolean("settings.allow_self_report", false);
        return new AbuseProtectionSettings(
                getConfig().getBoolean("abuse_protection.enabled", true),
                TimeParser.parse(getConfig().getString("abuse_protection.global_cooldown", "2m")).toMillis(),
                TimeParser.parse(getConfig().getString("abuse_protection.same_target_cooldown", "10m")).toMillis(),
                getConfig().getInt("abuse_protection.max_open_reports_per_player", 5),
                !allowSelf,
                getConfig().getBoolean("abuse_protection.block_duplicate_reports", true),
                TimeParser.parse(getConfig().getString("abuse_protection.duplicate_window", "30m")).toMillis(),
                getConfig().getInt("abuse_protection.false_report_threshold", 5),
                getConfig().getBoolean("settings.allow_reporting_staff", true));
    }

    private DiscordSettings discordSettings() {
        return new DiscordSettings(
                getConfig().getBoolean("discord.enabled", false),
                getConfig().getString("discord.webhook_url", ""),
                getConfig().getString("discord.username", "SentinelReports"),
                getConfig().getString("discord.avatar_url", ""),
                getConfig().getBoolean("discord.notify_new_report", true),
                getConfig().getBoolean("discord.notify_critical_report", true),
                getConfig().getBoolean("discord.notify_report_closed", true),
                getConfig().getBoolean("discord.notify_report_assigned", true),
                getConfig().getBoolean("discord.notify_false_report", true),
                getConfig().getLong("discord.cooldown_seconds", 10) * 1000L);
    }

    private List<ReportCategory> loadCategories() {
        List<ReportCategory> categories = new ArrayList<>();
        ConfigurationSection section = getConfig().getConfigurationSection("categories");
        if (section == null) {
            return categories;
        }
        for (String id : section.getKeys(false)) {
            String path = "categories." + id + ".";
            categories.add(new ReportCategory(
                    id,
                    getConfig().getString(path + "name", id),
                    getConfig().getString(path + "material", "PAPER"),
                    ReportPriority.parse(getConfig().getString(path + "priority", "MEDIUM")).orElse(ReportPriority.MEDIUM),
                    getConfig().getString(path + "permission", null),
                    getConfig().getString(path + "description", ""),
                    parseOptionalDuration(path + "cooldown"),
                    getConfig().getString(path + "discord_message", null),
                    getConfig().getBoolean(path + "enabled", true)
            ));
        }
        return categories;
    }

    private long parseOptionalDuration(String path) {
        String value = getConfig().getString(path);
        if (value == null || value.isBlank()) {
            return 0L;
        }
        return TimeParser.parse(value).toMillis();
    }

    private void saveResourceIfMissing(String resource) {
        if (!new java.io.File(getDataFolder(), resource).exists()) {
            saveResource(resource, false);
        }
    }

    public ReportService reportService() {
        return reportService;
    }

    public ReportSettings reportSettings() {
        return reportSettings;
    }

    public PaperMessages messages() {
        return messages;
    }

    public PaperSchedulerAdapter scheduler() {
        return scheduler;
    }

    public ReportGuiManager guiManager() {
        return guiManager;
    }

    public ReportSessionManager sessionManager() {
        return sessionManager;
    }

    public ReportHubService hubService() {
        return hubService;
    }

    public PaperPluginMessenger pluginMessenger() {
        return pluginMessenger;
    }
}
