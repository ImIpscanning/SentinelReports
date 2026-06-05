package com.imipscanning.sentinelreports.common.storage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class MigrationManager {
    private static final List<String> MIGRATIONS = List.of("V001__init.sql");
    private final DatabaseManager databaseManager;

    public MigrationManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void migrate() {
        try (Connection connection = databaseManager.connection()) {
            ensureMigrationTable(connection);
            for (String migration : MIGRATIONS.stream().sorted(Comparator.naturalOrder()).toList()) {
                if (applied(connection, migration)) {
                    continue;
                }
                executeMigration(connection, migration);
                markApplied(connection, migration);
            }
        } catch (SQLException | IOException ex) {
            throw new StorageException("Database migration failed", ex);
        }
    }

    private void ensureMigrationTable(Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS schema_migrations (
                      version VARCHAR(128) PRIMARY KEY,
                      applied_at BIGINT NOT NULL
                    )
                    """);
        }
    }

    private boolean applied(Connection connection, String version) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT version FROM schema_migrations WHERE version = ?")) {
            statement.setString(1, version);
            try (var result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private void markApplied(Connection connection, String version) throws SQLException {
        try (var statement = connection.prepareStatement("INSERT INTO schema_migrations(version, applied_at) VALUES(?, ?)")) {
            statement.setString(1, version);
            statement.setLong(2, System.currentTimeMillis());
            statement.executeUpdate();
        }
    }

    private void executeMigration(Connection connection, String migration) throws IOException, SQLException {
        String vendor = databaseManager.type() == DatabaseType.SQLITE ? "sqlite" : "mysql";
        String resource = "/db/migration/" + vendor + "/" + migration;
        try (var stream = getClass().getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IOException("Missing migration resource " + resource);
            }
            String sql = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .lines()
                    .reduce("", (left, right) -> left + "\n" + right);
            for (String statementSql : splitStatements(sql)) {
                String trimmed = statementSql.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                try (var statement = connection.createStatement()) {
                    statement.execute(trimmed);
                }
            }
        }
    }

    private List<String> splitStatements(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String line : sql.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("--") || trimmed.isEmpty()) {
                continue;
            }
            current.append(line).append('\n');
            if (trimmed.endsWith(";")) {
                statements.add(current.toString().replaceFirst(";\\s*$", ""));
                current.setLength(0);
            }
        }
        if (!current.isEmpty()) {
            statements.add(current.toString());
        }
        return statements;
    }
}
