package com.imipscanning.sentinelreports.common.service;

import com.imipscanning.sentinelreports.common.util.PlaceholderFormatter;

import java.util.List;
import java.util.Map;

public final class MessageService {
    private final Map<String, Object> values;

    public MessageService(Map<String, Object> values) {
        this.values = values == null ? Map.of() : Map.copyOf(values);
    }

    public String string(String path, Map<String, ?> placeholders, String fallback) {
        Object value = value(path);
        String raw = value instanceof String string ? string : fallback;
        return PlaceholderFormatter.format(raw, placeholders);
    }

    @SuppressWarnings("unchecked")
    public List<String> list(String path, Map<String, ?> placeholders, List<String> fallback) {
        Object value = value(path);
        List<String> raw = value instanceof List<?> list ? list.stream().map(String::valueOf).toList() : fallback;
        return raw.stream().map(line -> PlaceholderFormatter.format(line, placeholders)).toList();
    }

    @SuppressWarnings("unchecked")
    private Object value(String path) {
        String[] parts = path.split("\\.");
        Object current = values;
        for (String part : parts) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = ((Map<String, Object>) map).get(part);
        }
        return current;
    }
}
