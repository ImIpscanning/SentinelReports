package com.imipscanning.sentinelreports.paper.commands;

import java.util.List;

public final class HelpCommand extends AbstractSubCommand {
    private final CommandHelpProvider helpProvider;

    public HelpCommand(CommandHelpProvider helpProvider) {
        super("help", List.of(), "Muestra ayuda organizada por categorias.", "sentinelreports.staff",
                "help [page|player|staff|evidence|notes|history|admin]", List.of("/reports help staff"), "admin");
        this.helpProvider = helpProvider;
    }

    @Override
    public CommandResult execute(CommandContext context) {
        helpProvider.send(context, context.arg(1));
        return CommandResult.SUCCESS;
    }
}
