package com.imipscanning.sentinelreports.common.storage;

import java.util.Locale;

public enum DatabaseType {
    SQLITE,
    MYSQL;

    public static DatabaseType parse(String input) {
        if (input == null || input.isBlank()) {
            return SQLITE;
        }
        String normalized = input.trim().toUpperCase(Locale.ROOT);
        if (normalized.equals("MARIADB")) {
            return MYSQL;
        }
        return valueOf(normalized);
    }
}
