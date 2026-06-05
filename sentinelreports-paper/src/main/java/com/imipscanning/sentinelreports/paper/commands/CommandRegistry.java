package com.imipscanning.sentinelreports.paper.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class CommandRegistry {
    private final Map<String, SubCommand> commands = new LinkedHashMap<>();

    public void register(SubCommand command) {
        commands.put(command.name().toLowerCase(), command);
        for (String alias : command.aliases()) {
            commands.put(alias.toLowerCase(), command);
        }
    }

    public Optional<SubCommand> find(String name) {
        return Optional.ofNullable(commands.get(name.toLowerCase()));
    }

    public List<SubCommand> unique() {
        return new ArrayList<>(commands.values().stream()
                .distinct()
                .sorted(Comparator.comparing(SubCommand::category).thenComparing(SubCommand::name))
                .toList());
    }
}
