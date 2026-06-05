package com.imipscanning.sentinelreports.paper.platform;

import com.imipscanning.sentinelreports.common.util.ColorFormatter;
import com.imipscanning.sentinelreports.common.util.PlaceholderFormatter;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Map;

public final class PaperMessages {
    private final JavaPlugin plugin;
    private YamlConfiguration messages;

    public PaperMessages(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.messages = YamlConfiguration.loadConfiguration(file);
    }

    public String raw(String path, Map<String, ?> placeholders, String fallback) {
        return PlaceholderFormatter.format(messages.getString(path, fallback), placeholders);
    }

    public List<String> rawList(String path, Map<String, ?> placeholders, List<String> fallback) {
        List<String> lines = messages.getStringList(path);
        if (lines.isEmpty()) {
            lines = fallback;
        }
        return lines.stream().map(line -> PlaceholderFormatter.format(line, placeholders)).toList();
    }

    public void send(CommandSender sender, String path, Map<String, ?> placeholders, String fallback) {
        sender.sendMessage(ColorFormatter.component(raw(path, placeholders, fallback)));
    }

    public void sendList(CommandSender sender, String path, Map<String, ?> placeholders, List<String> fallback) {
        for (String line : rawList(path, placeholders, fallback)) {
            sender.sendMessage(ColorFormatter.component(line));
        }
    }

    public void line(CommandSender sender) {
        sender.sendMessage(ColorFormatter.component(messages.getString("decorations.line", "&8&m------------------------------------")));
    }
}
