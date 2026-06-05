package com.imipscanning.sentinelreports.paper.commands;

import com.imipscanning.sentinelreports.common.util.Pagination;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class CommandHelpProvider {
    private final CommandRegistry registry;

    public CommandHelpProvider(CommandRegistry registry) {
        this.registry = registry;
    }

    public void send(CommandContext context, String categoryOrPage) {
        List<SubCommand> commands = registry.unique().stream()
                .filter(command -> context.hasPermission(command.permission()))
                .toList();
        int page = 1;
        if (categoryOrPage != null && !categoryOrPage.isBlank()) {
            try {
                page = Integer.parseInt(categoryOrPage);
            } catch (NumberFormatException ignored) {
                String category = categoryOrPage.toLowerCase(Locale.ROOT);
                commands = commands.stream().filter(command -> command.category().equalsIgnoreCase(category)).toList();
            }
        }
        Pagination<SubCommand> pagination = Pagination.of(commands, page, 5);
        context.plugin().messages().line(context.sender());
        context.plugin().messages().send(context.sender(), "help.staff_title", Map.of(), "&b&lSentinelReports &8» &fAyuda Staff");
        context.plugin().messages().line(context.sender());
        for (SubCommand command : pagination.items()) {
            context.sender().sendMessage(com.imipscanning.sentinelreports.common.util.ColorFormatter.component("&b/" + context.label() + " " + command.syntax().usage()));
            context.sender().sendMessage(com.imipscanning.sentinelreports.common.util.ColorFormatter.component("&7" + command.description()));
            if (!command.syntax().examples().isEmpty()) {
                context.sender().sendMessage(com.imipscanning.sentinelreports.common.util.ColorFormatter.component("&8Ejemplo: &f" + command.syntax().examples().getFirst()));
            }
            context.sender().sendMessage(com.imipscanning.sentinelreports.common.util.ColorFormatter.component("&8Permiso: &f" + command.permission()));
            context.sender().sendMessage(com.imipscanning.sentinelreports.common.util.ColorFormatter.component(" "));
        }
        context.plugin().messages().line(context.sender());
        context.sender().sendMessage(com.imipscanning.sentinelreports.common.util.ColorFormatter.component("&7Pagina &f" + pagination.page() + "&7/&f" + pagination.totalPages() + " &8| &7Usa &f/" + context.label() + " help " + Math.min(pagination.page() + 1, pagination.totalPages())));
        context.plugin().messages().line(context.sender());
    }
}
