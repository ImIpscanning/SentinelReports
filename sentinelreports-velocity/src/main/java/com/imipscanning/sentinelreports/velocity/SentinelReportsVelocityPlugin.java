package com.imipscanning.sentinelreports.velocity;

import com.google.inject.Inject;
import com.imipscanning.sentinelreports.common.api.SentinelReportsProvider;
import com.imipscanning.sentinelreports.common.model.ReportCategory;
import com.imipscanning.sentinelreports.common.model.ReportPriority;
import com.imipscanning.sentinelreports.common.model.ReportStatus;
import com.imipscanning.sentinelreports.common.service.*;
import com.imipscanning.sentinelreports.common.storage.*;
import com.imipscanning.sentinelreports.common.util.TimeParser;
import com.imipscanning.sentinelreports.velocity.commands.VelocityReportCommand;
import com.imipscanning.sentinelreports.velocity.commands.VelocityReportsCommand;
import com.imipscanning.sentinelreports.velocity.platform.VelocityMessages;
import com.imipscanning.sentinelreports.velocity.platform.VelocityNotificationSink;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

@Plugin(
        id = "sentinelreports",
        name = "SentinelReports",
        version = "1.0.0",
        authors = {"ImIpscanning"}
)
public final class SentinelReportsVelocityPlugin {
    public static final ChannelIdentifier CHANNEL = MinecraftChannelIdentifier.from("sentinelreports:sync");

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Path dataDirectory;
    private final VelocityMessages messages = new VelocityMessages();
    private ConfigurationNode config;
    private DatabaseManager databaseManager;
    private ReportService reportService;

    @Inject
    public SentinelReportsVelocityPlugin(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        try {
            loadConfig();
            startStorage();
            buildServices();
            registerCommands();
            proxyServer.getChannelRegistrar().register(CHANNEL);
            SentinelReportsProvider.register(reportService);
            logger.info("SentinelReports Velocity enabled.");
        } catch (Exception ex) {
            logger.error("Could not enable SentinelReports Velocity", ex);
        }
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        SentinelReportsProvider.unregister(reportService);
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(CHANNEL)) {
            return;
        }
        event.setResult(PluginMessageEvent.ForwardResult.handled());
    }

    public void reloadRuntime() {
        try {
            loadConfig();
        } catch (IOException ex) {
            logger.warn("Could not reload config", ex);
        }
    }

    private void loadConfig() throws IOException {
        Files.createDirectories(dataDirectory);
        Path file = dataDirectory.resolve("config.yml");
        if (!Files.exists(file)) {
            try (InputStream stream = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                if (stream == null) {
                    throw new IOException("Missing default config.yml");
                }
                Files.copy(stream, file);
            }
        }
        config = YamlConfigurationLoader.builder().path(file).build().load();
    }

    private void startStorage() {
        databaseManager = new DatabaseManager(databaseSettings());
        databaseManager.start();
        new MigrationManager(databaseManager).migrate();
    }

    private void buildServices() {
        ReportRepository reportRepository = new ReportRepository(databaseManager);
        EvidenceRepository evidenceRepository = new EvidenceRepository(databaseManager);
        NoteRepository noteRepository = new NoteRepository(databaseManager);
        CategoryRepository categoryRepository = new CategoryRepository(databaseManager);
        ActionRepository actionRepository = new ActionRepository(databaseManager);
        categoryRepository.upsertAll(loadCategories());

        ReportSettings reportSettings = loadReportSettings();
        AbuseDetectionService abuseDetectionService = new AbuseDetectionService(abuseSettings(), new CooldownService(), reportRepository);
        ReportValidationService validationService = new ReportValidationService(reportSettings, categoryRepository, abuseDetectionService);
        Executor executor = command -> proxyServer.getScheduler().buildTask(this, command).schedule();
        DiscordWebhookService discord = new DiscordWebhookService(discordSettings(), executor);
        ReportNotificationService notification = new ReportNotificationService(new VelocityNotificationSink(proxyServer), List.of(
                "&8&m------------------------------------",
                "&c&lNuevo Reporte &8#%id%",
                "&7Reportado: &f%target%",
                "&7Reportador: &f%reporter%",
                "&7Categoria: &f%category%",
                "&7Prioridad: &f%priority%",
                "&7Servidor: &f%server%",
                "&7Razon: &f%reason%",
                "&8&m------------------------------------"
        ));
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
        var manager = proxyServer.getCommandManager();
        manager.register(manager.metaBuilder("report").aliases("rep").plugin(this).build(), new VelocityReportCommand(this));
        manager.register(manager.metaBuilder("reports").aliases("reportadmin", "rc", "sreports").plugin(this).build(), new VelocityReportsCommand(this));
    }

    private DatabaseSettings databaseSettings() {
        DatabaseType type = DatabaseType.parse(node("database", "type").getString("sqlite"));
        Path sqlite = dataDirectory.resolve(node("database", "sqlite_file").getString("sentinelreports.db"));
        return new DatabaseSettings(type, sqlite,
                node("database", "mysql", "host").getString("localhost"),
                node("database", "mysql", "port").getInt(3306),
                node("database", "mysql", "database").getString("sentinelreports"),
                node("database", "mysql", "username").getString("root"),
                node("database", "mysql", "password").getString("change-me"),
                node("database", "mysql", "pool_size").getInt(10));
    }

    private ReportSettings loadReportSettings() {
        return new ReportSettings(
                node("settings", "require_reason").getBoolean(true),
                60,
                ReportStatus.parse(node("reports", "default_status").getString("OPEN")).orElse(ReportStatus.OPEN),
                ReportPriority.parse(node("reports", "default_priority").getString("MEDIUM")).orElse(ReportPriority.MEDIUM),
                node("reports", "close_requires_reason").getBoolean(true),
                dataDirectory.resolve(node("reports", "export_folder").getString("exports")),
                node("settings", "server_name").getString("velocity"));
    }

    private AbuseProtectionSettings abuseSettings() {
        boolean allowSelf = node("settings", "allow_self_report").getBoolean(false);
        return new AbuseProtectionSettings(
                node("abuse_protection", "enabled").getBoolean(true),
                TimeParser.parse(node("abuse_protection", "global_cooldown").getString("2m")).toMillis(),
                TimeParser.parse(node("abuse_protection", "same_target_cooldown").getString("10m")).toMillis(),
                node("abuse_protection", "max_open_reports_per_player").getInt(5),
                !allowSelf,
                node("abuse_protection", "block_duplicate_reports").getBoolean(true),
                TimeParser.parse(node("abuse_protection", "duplicate_window").getString("30m")).toMillis(),
                node("abuse_protection", "false_report_threshold").getInt(5),
                node("settings", "allow_reporting_staff").getBoolean(true));
    }

    private DiscordSettings discordSettings() {
        return new DiscordSettings(
                node("discord", "enabled").getBoolean(false),
                node("discord", "webhook_url").getString(""),
                node("discord", "username").getString("SentinelReports"),
                node("discord", "avatar_url").getString(""),
                node("discord", "notify_new_report").getBoolean(true),
                node("discord", "notify_critical_report").getBoolean(true),
                node("discord", "notify_report_closed").getBoolean(true),
                node("discord", "notify_report_assigned").getBoolean(true),
                node("discord", "notify_false_report").getBoolean(true),
                node("discord", "cooldown_seconds").getLong(10) * 1000L);
    }

    private List<ReportCategory> loadCategories() {
        List<ReportCategory> categories = new ArrayList<>();
        ConfigurationNode categoriesNode = node("categories");
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : categoriesNode.childrenMap().entrySet()) {
            String id = String.valueOf(entry.getKey());
            ConfigurationNode category = entry.getValue();
            categories.add(new ReportCategory(id,
                    category.node("name").getString(id),
                    category.node("material").getString("PAPER"),
                    ReportPriority.parse(category.node("priority").getString("MEDIUM")).orElse(ReportPriority.MEDIUM),
                    category.node("permission").getString(null),
                    category.node("description").getString(""),
                    0L,
                    category.node("discord_message").getString(null),
                    category.node("enabled").getBoolean(true)));
        }
        return categories;
    }

    private ConfigurationNode node(Object... path) {
        return config.node(path);
    }

    public ProxyServer proxyServer() {
        return proxyServer;
    }

    public VelocityMessages messages() {
        return messages;
    }

    public ReportService reportService() {
        return reportService;
    }

    public ReportSettings reportSettings() {
        return loadReportSettings();
    }
}
