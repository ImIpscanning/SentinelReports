package com.imipscanning.sentinelreports.paper.commands;

import java.util.List;
import java.util.Map;

public final class DiscordTestCommand extends AbstractSubCommand {
    public DiscordTestCommand() {
        super("discord", List.of(), "Prueba el webhook de Discord.", "sentinelreports.staff.discord",
                "discord test", List.of("/reports discord test"), "admin");
    }

    @Override
    public CommandResult execute(CommandContext context) {
        if (!context.arg(1).equalsIgnoreCase("test")) {
            throw new CommandException("errors.invalid_command", "Invalid discord action", syntax().usage(), syntax().examples().getFirst());
        }
        context.async(() -> {
            context.plugin().reportService().discord().sendTest().join();
            return true;
        }, ignored -> context.reply("success.discord_test", Map.of(), "&aTest de Discord enviado."));
        return CommandResult.SUCCESS;
    }

    @Override
    public List<String> tabComplete(CommandContext context) {
        if (context.args().length == 2) {
            return List.of("test");
        }
        return List.of();
    }
}
