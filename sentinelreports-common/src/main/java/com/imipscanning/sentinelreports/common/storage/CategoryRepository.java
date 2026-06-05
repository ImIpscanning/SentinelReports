package com.imipscanning.sentinelreports.common.storage;

import com.imipscanning.sentinelreports.common.model.ReportCategory;
import com.imipscanning.sentinelreports.common.model.ReportPriority;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class CategoryRepository {
    private final DatabaseManager databaseManager;
    private final Map<String, ReportCategory> cache = new LinkedHashMap<>();

    public CategoryRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void upsertAll(List<ReportCategory> categories) {
        for (ReportCategory category : categories) {
            upsert(category);
        }
        reload();
    }

    public void upsert(ReportCategory category) {
        String sql = databaseManager.type() == DatabaseType.SQLITE
                ? """
                INSERT INTO report_categories(id, name, priority, enabled, created_at)
                VALUES(?,?,?,?,?)
                ON CONFLICT(id) DO UPDATE SET name = excluded.name, priority = excluded.priority, enabled = excluded.enabled
                """
                : """
                INSERT INTO report_categories(id, name, priority, enabled, created_at)
                VALUES(?,?,?,?,?)
                ON DUPLICATE KEY UPDATE name = VALUES(name), priority = VALUES(priority), enabled = VALUES(enabled)
                """;
        try (Connection connection = databaseManager.connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, category.id());
            statement.setString(2, category.name());
            statement.setString(3, category.defaultPriority().name());
            statement.setBoolean(4, category.enabled());
            statement.setLong(5, System.currentTimeMillis());
            statement.executeUpdate();
        } catch (Exception ex) {
            throw new StorageException("Could not save category " + category.id(), ex);
        }
    }

    public void reload() {
        cache.clear();
        for (ReportCategory category : loadAll()) {
            if (category.enabled()) {
                cache.put(category.id(), category);
            }
        }
    }

    public List<ReportCategory> loadAll() {
        try (Connection connection = databaseManager.connection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM report_categories ORDER BY id ASC");
             var rs = statement.executeQuery()) {
            List<ReportCategory> categories = new ArrayList<>();
            while (rs.next()) {
                categories.add(new ReportCategory(
                        rs.getString("id"),
                        rs.getString("name"),
                        "PAPER",
                        ReportPriority.parse(rs.getString("priority")).orElse(ReportPriority.MEDIUM),
                        null,
                        "",
                        0L,
                        null,
                        rs.getBoolean("enabled")
                ));
            }
            return categories;
        } catch (Exception ex) {
            throw new StorageException("Could not load categories", ex);
        }
    }

    public Optional<ReportCategory> find(String id) {
        if (cache.isEmpty()) {
            reload();
        }
        return Optional.ofNullable(cache.get(id == null ? "" : id.toLowerCase()));
    }

    public List<ReportCategory> enabled() {
        if (cache.isEmpty()) {
            reload();
        }
        return List.copyOf(cache.values());
    }
}
