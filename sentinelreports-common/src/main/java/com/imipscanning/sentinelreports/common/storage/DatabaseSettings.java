package com.imipscanning.sentinelreports.common.storage;

import java.nio.file.Path;

public record DatabaseSettings(
        DatabaseType type,
        Path sqliteFile,
        String host,
        int port,
        String database,
        String username,
        String password,
        int poolSize
) {
    public DatabaseSettings {
        type = type == null ? DatabaseType.SQLITE : type;
        port = port <= 0 ? 3306 : port;
        database = database == null || database.isBlank() ? "sentinelreports" : database;
        username = username == null ? "" : username;
        password = password == null ? "" : password;
        poolSize = Math.max(1, poolSize);
    }
}
