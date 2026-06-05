package com.imipscanning.sentinelreports.paper.commands;

import java.util.List;

@FunctionalInterface
public interface TabCompletionProvider {
    List<String> tabComplete(CommandContext context);
}
