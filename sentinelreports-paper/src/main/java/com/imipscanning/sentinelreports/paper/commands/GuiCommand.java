package com.imipscanning.sentinelreports.paper.commands;

import java.util.List;

public final class GuiCommand extends AbstractSubCommand {
    public GuiCommand() {
        super("gui", List.of("menu"), "Abre el panel staff.", "sentinelreports.staff",
                "gui", List.of("/reports gui"), "staff");
    }

    @Override
    public CommandResult execute(CommandContext context) {
        context.plugin().guiManager().openStaff(context.player());
        return CommandResult.SUCCESS;
    }
}
