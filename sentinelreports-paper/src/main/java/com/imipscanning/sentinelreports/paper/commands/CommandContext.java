package com.imipscanning.sentinelreports.paper.commands;

import com.imipscanning.sentinelreports.common.service.ValidationException;
import com.imipscanning.sentinelreports.common.util.TimeFormatter;
import com.imipscanning.sentinelreports.paper.SentinelReportsPaperPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public record CommandContext(
        SentinelReportsPaperPlugin plugin,
        CommandSender sender,
        String label,
        String[] args
) {
    public boolean isPlayer() {
        return sender instanceof Player;
    }

    public Player player() {
        if (!(sender instanceof Player player)) {
            throw new CommandException("errors.player_only", "Only players can use this command", "/" + label, "/" + label);
        }
        return player;
    }

    public boolean hasPermission(String permission) {
        return permission == null || permission.isBlank() || sender.hasPermission(permission) || sender.hasPermission("sentinelreports.admin");
    }

    public void requirePermission(String permission) {
        if (!hasPermission(permission)) {
            throw new CommandException("errors.no_permission", "No permission", "/" + label, "/" + label);
        }
    }

    public String arg(int index) {
        return index >= 0 && index < args.length ? args[index] : "";
    }

    public String joined(int start) {
        if (start >= args.length) {
            return "";
        }
        return String.join(" ", Arrays.copyOfRange(args, start, args.length));
    }

    public void reply(String path, Map<String, ?> placeholders, String fallback) {
        plugin.messages().send(sender, path, placeholders, fallback);
    }

    public void replyList(String path, Map<String, ?> placeholders, List<String> fallback) {
        plugin.messages().sendList(sender, path, placeholders, fallback);
    }

    public <T> void async(Supplier<T> supplier, Consumer<T> success) {
        plugin.scheduler().runAsync(() -> {
            try {
                T value = supplier.get();
                plugin.scheduler().runGlobal(() -> success.accept(value));
            } catch (ValidationException ex) {
                plugin.scheduler().runGlobal(() -> handleValidation(ex));
            } catch (Exception ex) {
                plugin.getLogger().warning("Command failed: " + ex.getMessage());
                plugin.scheduler().runGlobal(() -> reply("errors.internal", Map.of(), "&cOcurrio un error interno controlado."));
            }
        });
    }

    public void asyncRun(Runnable runnable, Runnable success) {
        async(() -> {
            runnable.run();
            return true;
        }, ignored -> success.run());
    }

    public List<String> onlinePlayerNames(String prefix) {
        String lower = prefix == null ? "" : prefix.toLowerCase();
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(lower))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public UUID senderUuidOrConsole() {
        return sender instanceof Player player ? player.getUniqueId() : new UUID(0L, 0L);
    }

    public String senderName() {
        return sender instanceof Player player ? player.getName() : "Console";
    }

    private void handleValidation(ValidationException ex) {
        long remaining = 0L;
        try {
            remaining = Long.parseLong(ex.getMessage());
        } catch (NumberFormatException ignored) {
        }
        reply(ex.messageKey(), Map.of("time", TimeFormatter.compact(Duration.ofMillis(remaining))), "&c" + ex.getMessage());
    }
}
