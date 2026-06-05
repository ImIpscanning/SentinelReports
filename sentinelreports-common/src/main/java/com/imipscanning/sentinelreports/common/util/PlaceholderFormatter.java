package com.imipscanning.sentinelreports.common.util;

import java.util.Map;

public final class PlaceholderFormatter {
    private PlaceholderFormatter() {
    }

    public static String format(String input, Map<String, ?> placeholders) {
        if (input == null || input.isEmpty() || placeholders == null || placeholders.isEmpty()) {
            return input == null ? "" : input;
        }
        String result = input;
        for (Map.Entry<String, ?> entry : placeholders.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            result = result.replace("%" + key + "%", value == null ? "" : String.valueOf(value));
        }
        return result;
    }
}
