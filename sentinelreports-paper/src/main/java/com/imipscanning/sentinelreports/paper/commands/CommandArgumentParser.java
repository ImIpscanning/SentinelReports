package com.imipscanning.sentinelreports.paper.commands;

import java.util.Optional;

public final class CommandArgumentParser {
    private CommandArgumentParser() {
    }

    public static Optional<Long> reportId(String input) {
        return com.imipscanning.sentinelreports.common.util.CommandArgumentParser.positiveLong(input);
    }

    public static String join(String[] args, int start) {
        return com.imipscanning.sentinelreports.common.util.CommandArgumentParser.join(args, start);
    }
}
