package com.imipscanning.sentinelreports.paper.commands;

import java.util.List;
import java.util.Map;

public final class ReloadCommand extends AbstractSubCommand {
    public ReloadCommand() {
        super("reload", List.of(), "Recarga configuracion, mensajes, GUI y categorias.", "sentinelreports.staff.reload",
                "reload", List.of("/reports reload"), "admin");
    }

    @Override
    public CommandResult execute(CommandContext context) {
        context.plugin().reloadRuntime();
        context.reply("success.reloaded", Map.of(), "&aConfiguracion recargada correctamente.");
        return CommandResult.SUCCESS;
    }
}
