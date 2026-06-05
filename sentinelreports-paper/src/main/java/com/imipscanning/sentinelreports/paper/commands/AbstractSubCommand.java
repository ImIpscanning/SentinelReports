package com.imipscanning.sentinelreports.paper.commands;

import java.util.List;

public abstract class AbstractSubCommand implements SubCommand {
    private final String name;
    private final List<String> aliases;
    private final String description;
    private final String permission;
    private final CommandSyntax syntax;
    private final String category;

    protected AbstractSubCommand(String name, List<String> aliases, String description, String permission, String usage, List<String> examples, String category) {
        this.name = name;
        this.aliases = aliases == null ? List.of() : List.copyOf(aliases);
        this.description = description;
        this.permission = permission;
        this.syntax = new CommandSyntax(usage, examples);
        this.category = category;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<String> aliases() {
        return aliases;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public String permission() {
        return permission;
    }

    @Override
    public CommandSyntax syntax() {
        return syntax;
    }

    @Override
    public String category() {
        return category;
    }

    protected long requireId(CommandContext context, int index) {
        return CommandArgumentParser.reportId(context.arg(index))
                .orElseThrow(() -> new CommandException("errors.invalid_report", "Invalid report id", syntax().usage(), syntax().examples().isEmpty() ? "" : syntax().examples().getFirst()));
    }
}
