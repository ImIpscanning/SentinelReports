package com.imipscanning.sentinelreports.common.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

public final class StringSanitizer {
    private static final int MAX_REASON = 512;
    private static final int MAX_NOTE = 2_000;
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    private StringSanitizer() {
    }

    public static String reason(String input) {
        return limit(clean(input), MAX_REASON);
    }

    public static String note(String input) {
        return limit(clean(input), MAX_NOTE);
    }

    public static String clean(String input) {
        if (input == null) {
            return "";
        }
        return input.replace('\n', ' ').replace('\r', ' ').trim();
    }

    public static String limit(String input, int maxLength) {
        String value = input == null ? "" : input;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    public static boolean safeEvidence(String input) {
        String value = clean(input);
        if (value.isBlank()) {
            return false;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            try {
                URI uri = new URI(value);
                return uri.getScheme() != null
                        && ALLOWED_SCHEMES.contains(uri.getScheme().toLowerCase(Locale.ROOT))
                        && uri.getHost() != null
                        && value.length() <= 1_000;
            } catch (URISyntaxException ex) {
                return false;
            }
        }
        return value.length() <= 1_000 && !lower.contains("discord.com/api/webhooks/");
    }
}
