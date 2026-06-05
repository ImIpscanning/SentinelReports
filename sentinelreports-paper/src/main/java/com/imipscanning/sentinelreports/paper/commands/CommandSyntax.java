package com.imipscanning.sentinelreports.paper.commands;

import java.util.List;

public record CommandSyntax(String usage, List<String> examples) {
    public CommandSyntax {
        examples = examples == null ? List.of() : List.copyOf(examples);
    }
}
