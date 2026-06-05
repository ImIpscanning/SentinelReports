package com.imipscanning.sentinelreports.paper.commands;

import java.util.List;

public interface SubCommand {
    String name();

    default List<String> aliases() {
        return List.of();
    }

    String description();

    String permission();

    CommandSyntax syntax();

    String category();

    CommandResult execute(CommandContext context);

    default List<String> tabComplete(CommandContext context) {
        return List.of();
    }

    default boolean matches(String input) {
        return name().equalsIgnoreCase(input) || aliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(input));
    }
}
