package com.imipscanning.sentinelreports.paper.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class ReportsCommand implements CommandExecutor, TabCompleter {
    private final com.imipscanning.sentinelreports.paper.SentinelReportsPaperPlugin plugin;
    private final CommandRegistry registry = new CommandRegistry();
    private final CommandHelpProvider helpProvider = new CommandHelpProvider(registry);

    public ReportsCommand(com.imipscanning.sentinelreports.paper.SentinelReportsPaperPlugin plugin) {
        this.plugin = plugin;
        register();
    }

    private void register() {
        registry.register(new HelpCommand(helpProvider));
        registry.register(new GuiCommand());
        registry.register(new HubCommand());
        registry.register(new ListReportsCommand());
        registry.register(new ViewReportCommand());
        registry.register(new ClaimReportCommand());
        registry.register(new UnclaimReportCommand());
        registry.register(new AssignReportCommand());
        registry.register(new StatusReportCommand());
        registry.register(new PriorityReportCommand());
        registry.register(new CloseReportCommand());
        registry.register(new RejectReportCommand());
        registry.register(new FalseReportCommand());
        registry.register(new EvidenceCommand());
        registry.register(new NoteCommand());
        registry.register(new HistoryCommand());
        registry.register(new HistoryCommand("against", HistoryCommand.Mode.AGAINST, "Lista reportes contra un jugador.", "against <player>"));
        registry.register(new HistoryCommand("by", HistoryCommand.Mode.BY, "Lista reportes creados por un jugador.", "by <player>"));
        registry.register(new StatsCommand());
        registry.register(new ExportCommand());
        registry.register(new ReloadCommand());
        registry.register(new DiscordTestCommand());
        registry.register(new TeleportCommand());
        registry.register(new ServerCommand());
        registry.register(new NotifyCommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        CommandContext context = new CommandContext(plugin, sender, label, args);
        try {
            if (args.length == 0) {
                plugin.guiManager().openStaff(context.player());
                return true;
            }
            String name = args[0];
            if (name.equalsIgnoreCase("menu")) {
                name = "gui";
            }
            SubCommand subCommand = registry.find(name).orElse(null);
            if (subCommand == null) {
                helpProvider.send(context, "");
                return true;
            }
            context.requirePermission(subCommand.permission());
            subCommand.execute(context);
        } catch (CommandException ex) {
            sendCommandError(context, ex);
        }
        return true;
    }

    private void sendCommandError(CommandContext context, CommandException ex) {
        plugin.messages().line(context.sender());
        context.sender().sendMessage(com.imipscanning.sentinelreports.common.util.ColorFormatter.component("&c&lComando invalido"));
        plugin.messages().line(context.sender());
        plugin.messages().send(context.sender(), ex.messageKey(), Map.of(), "&c" + ex.getMessage());
        context.sender().sendMessage(com.imipscanning.sentinelreports.common.util.ColorFormatter.component("&7Uso correcto:"));
        context.sender().sendMessage(com.imipscanning.sentinelreports.common.util.ColorFormatter.component("&f/reports " + ex.syntax()));
        if (!ex.example().isBlank()) {
            context.sender().sendMessage(com.imipscanning.sentinelreports.common.util.ColorFormatter.component("&7Ejemplo:"));
            context.sender().sendMessage(com.imipscanning.sentinelreports.common.util.ColorFormatter.component("&f" + ex.example()));
        }
        plugin.messages().line(context.sender());
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        CommandContext context = new CommandContext(plugin, sender, label, args);
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return registry.unique().stream()
                    .filter(sub -> context.hasPermission(sub.permission()))
                    .flatMap(sub -> java.util.stream.Stream.concat(java.util.stream.Stream.of(sub.name()), sub.aliases().stream()))
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
        }
        return registry.find(args[0]).map(sub -> sub.tabComplete(context)).orElse(List.of());
    }
}
