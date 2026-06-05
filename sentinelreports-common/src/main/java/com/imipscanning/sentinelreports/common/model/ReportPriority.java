package com.imipscanning.sentinelreports.common.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum ReportPriority {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int weight;

    ReportPriority(int weight) {
        this.weight = weight;
    }

    public int weight() {
        return weight;
    }

    public static Optional<ReportPriority> parse(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }
        String normalized = input.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        return Arrays.stream(values()).filter(priority -> priority.name().equals(normalized)).findFirst();
    }
}
