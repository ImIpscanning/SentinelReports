package com.imipscanning.sentinelreports.paper.commands;

import com.imipscanning.sentinelreports.common.model.ReportHubButton;
import com.imipscanning.sentinelreports.common.util.ColorFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.List;
import java.util.Map;

public final class HubCommand extends AbstractSubCommand {
    public HubCommand() {
        super("hub", List.of(), "Muestra un panel interactivo en chat.", "sentinelreports.staff",
                "hub", List.of("/reports hub"), "staff");
    }

    @Override
    public CommandResult execute(CommandContext context) {
        context.plugin().messages().line(context.sender());
        context.plugin().messages().send(context.sender(), "hub.staff_title", Map.of(), "&b&lSentinelReports &8» &fPanel Staff");
        context.plugin().messages().line(context.sender());
        boolean showOnlyAllowed = context.plugin().getConfig().getBoolean("hub.show_only_allowed_buttons", true);
        for (ReportHubButton button : context.plugin().hubService().staffButtons()) {
            if (showOnlyAllowed && button.permission() != null && !context.hasPermission(button.permission())) {
                continue;
            }
            Component line = ColorFormatter.component("&b[" + button.name() + "] &7" + button.description())
                    .hoverEvent(HoverEvent.showText(ColorFormatter.component("&f" + button.description() + "\n&8Permiso: &7" + button.permission())))
                    .clickEvent(button.suggestCommand() ? ClickEvent.suggestCommand(button.command()) : ClickEvent.runCommand(button.command()));
            context.sender().sendMessage(line);
        }
        context.plugin().messages().line(context.sender());
        return CommandResult.SUCCESS;
    }
}
