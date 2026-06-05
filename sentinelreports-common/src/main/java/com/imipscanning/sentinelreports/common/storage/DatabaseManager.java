package com.imipscanning.sentinelreports.common.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseManager implements Closeable {
    private final DatabaseSettings settings;
    private HikariDataSource dataSource;

    public DatabaseManager(DatabaseSettings settings) {
        this.settings = settings;
    }

    public void start() {
        if (dataSource != null) {
            return;
        }
        HikariConfig config = new HikariConfig();
        config.setPoolName("SentinelReports");
        config.setMaximumPoolSize(settings.poolSize());
        config.setMinimumIdle(Math.min(2, settings.poolSize()));
        config.setConnectionTimeout(10_000);
        config.setValidationTimeout(5_000);
        config.setLeakDetectionThreshold(30_000);
        if (settings.type() == DatabaseType.SQLITE) {
            try {
                if (settings.sqliteFile().getParent() != null) {
                    Files.createDirectories(settings.sqliteFile().getParent());
                }
            } catch (IOException ex) {
                throw new StorageException("Could not create SQLite directory", ex);
            }
            config.setJdbcUrl("jdbc:sqlite:" + settings.sqliteFile().toAbsolutePath());
            config.setDriverClassName("org.sqlite.JDBC");
            config.setMaximumPoolSize(1);
            config.addDataSourceProperty("foreign_keys", "true");
        } else {
            config.setJdbcUrl("jdbc:mysql://" + settings.host() + ":" + settings.port() + "/" + settings.database()
                    + "?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC");
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setUsername(settings.username());
            config.setPassword(settings.password());
        }
        dataSource = new HikariDataSource(config);
    }

    public Connection connection() throws SQLException {
        if (dataSource == null) {
            throw new StorageException("Database has not been started");
        }
        Connection connection = dataSource.getConnection();
        if (settings.type() == DatabaseType.SQLITE) {
            try (var statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys=ON");
            }
        }
        return connection;
    }

    public DatabaseType type() {
        return settings.type();
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }
}
