package com.imipscanning.sentinelreports.common.util;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class CommandArgumentParser {
    private CommandArgumentParser() {
    }

    public static Optional<Long> positiveLong(String input) {
        try {
            long value = Long.parseLong(input);
            return value > 0 ? Optional.of(value) : Optional.empty();
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    public static Optional<UUID> uuid(String input) {
        try {
            return Optional.of(UUID.fromString(input));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public static String join(String[] args, int start) {
        if (args == null || start >= args.length) {
            return "";
        }
        return String.join(" ", Arrays.copyOfRange(args, Math.max(0, start), args.length)).trim();
    }

    public static String normalizeKey(String input) {
        return input == null ? "" : input.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }
}
