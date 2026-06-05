package com.imipscanning.sentinelreports.common.util;

import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeParser {
    private static final Pattern TOKEN = Pattern.compile("(\\d+)\\s*(ms|s|m|h|d|w)", Pattern.CASE_INSENSITIVE);

    private TimeParser() {
    }

    public static Duration parse(String input) {
        if (input == null || input.isBlank()) {
            return Duration.ZERO;
        }
        String normalized = input.trim().toLowerCase(Locale.ROOT);
        if (normalized.matches("\\d+")) {
            return Duration.ofSeconds(Long.parseLong(normalized));
        }
        Matcher matcher = TOKEN.matcher(normalized);
        Duration result = Duration.ZERO;
        int consumed = 0;
        while (matcher.find()) {
            long amount = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2).toLowerCase(Locale.ROOT);
            result = result.plus(switch (unit) {
                case "ms" -> Duration.ofMillis(amount);
                case "s" -> Duration.ofSeconds(amount);
                case "m" -> Duration.ofMinutes(amount);
                case "h" -> Duration.ofHours(amount);
                case "d" -> Duration.ofDays(amount);
                case "w" -> Duration.ofDays(amount * 7);
                default -> Duration.ZERO;
            });
            consumed += matcher.group().replace(" ", "").length();
        }
        String compact = normalized.replace(" ", "");
        if (result.isZero() || consumed != compact.length()) {
            throw new IllegalArgumentException("Invalid duration: " + input);
        }
        return result;
    }
}
