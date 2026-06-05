package com.imipscanning.sentinelreports.common.storage;

import com.imipscanning.sentinelreports.common.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ReportRepository {
    private final DatabaseManager databaseManager;

    public ReportRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Report create(Report report) {
        String sql = """
                INSERT INTO reports(report_uuid, reporter_uuid, reporter_name, target_uuid, target_name, category, reason,
                status, priority, server_name, world, x, y, z, assigned_to_uuid, assigned_to_name, assigned_at, created_at,
                updated_at, closed_at, closed_by_uuid, closed_by_name, close_reason)
                VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;
        try (Connection connection = databaseManager.connection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindReportInsert(statement, report);
            statement.executeUpdate();
            try (var keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return report.withId(keys.getLong(1));
                }
            }
            throw new StorageException("Report insert did not return generated id");
        } catch (SQLException ex) {
            throw new StorageException("Could not create report", ex);
        }
    }

    public Optional<Report> findById(long id) {
        try (Connection connection = databaseManager.connection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM reports WHERE id = ?")) {
            statement.setLong(1, id);
            try (var rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(JdbcMapper.report(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new StorageException("Could not load report " + id, ex);
        }
    }

    public List<Report> list(ReportQuery query) {
        SqlAndParams built = buildListSql(query, false);
        try (Connection connection = databaseManager.connection();
             PreparedStatement statement = connection.prepareStatement(built.sql())) {
            built.bind(statement);
            try (var rs = statement.executeQuery()) {
                List<Report> reports = new ArrayList<>();
                while (rs.next()) {
                    reports.add(JdbcMapper.report(rs));
                }
                return reports;
            }
        } catch (SQLException ex) {
            throw new StorageException("Could not list reports", ex);
        }
    }

    public long count(ReportFilter filter) {
        SqlAndParams built = buildCountSql(filter);
        try (Connection connection = databaseManager.connection();
             PreparedStatement statement = connection.prepareStatement(built.sql())) {
            built.bind(statement);
            try (var rs = statement.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException ex) {
            throw new StorageException("Could not count reports", ex);
        }
    }

    public void updateStatus(Report report) {
        String sql = """
                UPDATE reports SET status = ?, updated_at = ?, closed_at = ?, closed_by_uuid = ?, closed_by_name = ?,
                close_reason = ? WHERE id = ?
                """;
        try (Connection connection = databaseManager.connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, report.status().name());
            statement.setLong(2, report.updatedAt());
            setNullableLong(statement, 3, report.closedAt());
            statement.setString(4, JdbcMapper.uuid(report.closedByUuid()));
            statement.setString(5, report.closedByName());
            statement.setString(6, report.closeReason());
            statement.setLong(7, report.id());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new StorageException("Could not update report status", ex);
        }
    }

    public void updatePriority(long reportId, ReportPriority priority, long updatedAt) {
        try (Connection connection = databaseManager.connection();
             PreparedStatement statement = connection.prepareStatement("UPDATE reports SET priority = ?, updated_at = ? WHERE id = ?")) {
            statement.setString(1, priority.name());
            statement.setLong(2, updatedAt);
            statement.setLong(3, reportId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new StorageException("Could not update report priority", ex);
        }
    }

    public void assign(long reportId, UUID staffUuid, String staffName, long assignedAt) {
        String sql = """
                UPDATE reports SET status = ?, assigned_to_uuid = ?, assigned_to_name = ?, assigned_at = ?, updated_at = ?
                WHERE id = ?
                """;
        try (Connection connection = databaseManager.connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, staffUuid == null ? ReportStatus.OPEN.name() : ReportStatus.ASSIGNED.name());
            statement.setString(2, JdbcMapper.uuid(staffUuid));
            statement.setString(3, staffName);
            statement.setLong(4, staffUuid == null ? 0L : assignedAt);
            statement.setLong(5, assignedAt);
            statement.setLong(6, reportId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new StorageException("Could not assign report", ex);
        }
    }

    public long countOpenByReporter(UUID reporterUuid) {
        ReportFilter filter = new ReportFilter(null, null, null, null, reporterUuid, null, null, 0L, 0L);
        try (Connection connection = databaseManager.connection();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT COUNT(*) FROM reports
                     WHERE reporter_uuid = ? AND status IN ('OPEN','ASSIGNED','REVIEWING','WAITING_EVIDENCE')
                     """)) {
            statement.setString(1, reporterUuid.toString());
            try (var rs = statement.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException ex) {
            throw new StorageException("Could not count open reports for reporter", ex);
        }
    }

    public boolean hasRecentDuplicate(UUID reporterUuid, UUID targetUuid, String category, long after) {
        String sql = """
                SELECT id FROM reports
                WHERE reporter_uuid = ? AND target_uuid = ? AND category = ? AND created_at >= ?
                AND status IN ('OPEN','ASSIGNED','REVIEWING','WAITING_EVIDENCE')
                LIMIT 1
                """;
        try (Connection connection = databaseManager.connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, reporterUuid.toString());
            statement.setString(2, targetUuid.toString());
            statement.setString(3, category);
            statement.setLong(4, after);
            try (var rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new StorageException("Could not check duplicate report", ex);
        }
    }

    public PlayerReportStats playerStats(UUID playerUuid, String playerName) {
        try (Connection connection = databaseManager.connection()) {
            long created = scalar(connection, "SELECT COUNT(*) FROM reports WHERE reporter_uuid = ?", playerUuid);
            long against = scalar(connection, "SELECT COUNT(*) FROM reports WHERE target_uuid = ?", playerUuid);
            long open = scalar(connection, "SELECT COUNT(*) FROM reports WHERE target_uuid = ? AND status IN ('OPEN','ASSIGNED','REVIEWING','WAITING_EVIDENCE')", playerUuid);
            long falseReports = scalar(connection, "SELECT COUNT(*) FROM reports WHERE reporter_uuid = ? AND status = 'FALSE_REPORT'", playerUuid);
            long resolved = scalar(connection, "SELECT COUNT(*) FROM reports WHERE target_uuid = ? AND status = 'RESOLVED'", playerUuid);
            return new PlayerReportStats(playerUuid, playerName, created, against, open, falseReports, resolved);
        } catch (SQLException ex) {
            throw new StorageException("Could not load player stats", ex);
        }
    }

    private long scalar(Connection connection, String sql, UUID uuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (var rs = statement.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        }
    }

    private void bindReportInsert(PreparedStatement statement, Report report) throws SQLException {
        statement.setString(1, report.reportUuid().toString());
        statement.setString(2, report.reporterUuid().toString());
        statement.setString(3, report.reporterName());
        statement.setString(4, report.targetUuid().toString());
        statement.setString(5, report.targetName());
        statement.setString(6, report.category());
        statement.setString(7, report.reason());
        statement.setString(8, report.status().name());
        statement.setString(9, report.priority().name());
        statement.setString(10, report.serverName());
        statement.setString(11, report.world());
        setNullableDouble(statement, 12, report.x());
        setNullableDouble(statement, 13, report.y());
        setNullableDouble(statement, 14, report.z());
        statement.setString(15, JdbcMapper.uuid(report.assignedToUuid()));
        statement.setString(16, report.assignedToName());
        statement.setLong(17, report.assignedAt());
        statement.setLong(18, report.createdAt());
        statement.setLong(19, report.updatedAt());
        setNullableLong(statement, 20, report.closedAt());
        statement.setString(21, JdbcMapper.uuid(report.closedByUuid()));
        statement.setString(22, report.closedByName());
        statement.setString(23, report.closeReason());
    }

    private SqlAndParams buildListSql(ReportQuery query, boolean count) {
        String sortBy = switch (query.sortBy()) {
            case "id", "created_at", "updated_at", "priority", "status" -> query.sortBy();
            default -> "created_at";
        };
        SqlAndParams where = where(query.filter(), "SELECT * FROM reports");
        String sql = where.sql() + " ORDER BY " + sortBy + (query.descending() ? " DESC" : " ASC") + " LIMIT ? OFFSET ?";
        List<Object> params = new ArrayList<>(where.params());
        params.add(query.pageSize());
        params.add(query.offset());
        return new SqlAndParams(sql, params);
    }

    private SqlAndParams buildCountSql(ReportFilter filter) {
        return where(filter, "SELECT COUNT(*) FROM reports");
    }

    private SqlAndParams where(ReportFilter filter, String base) {
        List<String> clauses = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        if (filter != null) {
            if (filter.status() != null) {
                clauses.add("status = ?");
                params.add(filter.status().name());
            }
            if (filter.priority() != null) {
                clauses.add("priority = ?");
                params.add(filter.priority().name());
            }
            if (filter.category() != null && !filter.category().isBlank()) {
                clauses.add("category = ?");
                params.add(filter.category());
            }
            if (filter.serverName() != null && !filter.serverName().isBlank()) {
                clauses.add("server_name = ?");
                params.add(filter.serverName());
            }
            if (filter.reporterUuid() != null) {
                clauses.add("reporter_uuid = ?");
                params.add(filter.reporterUuid().toString());
            }
            if (filter.targetUuid() != null) {
                clauses.add("target_uuid = ?");
                params.add(filter.targetUuid().toString());
            }
            if (filter.assignedToUuid() != null) {
                clauses.add("assigned_to_uuid = ?");
                params.add(filter.assignedToUuid().toString());
            }
            if (filter.createdAfter() > 0) {
                clauses.add("created_at >= ?");
                params.add(filter.createdAfter());
            }
            if (filter.createdBefore() > 0) {
                clauses.add("created_at <= ?");
                params.add(filter.createdBefore());
            }
        }
        String sql = base + (clauses.isEmpty() ? "" : " WHERE " + String.join(" AND ", clauses));
        return new SqlAndParams(sql, params);
    }

    private void setNullableLong(PreparedStatement statement, int index, Long value) throws SQLException {
        if (value == null) {
            statement.setObject(index, null);
        } else {
            statement.setLong(index, value);
        }
    }

    private void setNullableDouble(PreparedStatement statement, int index, Double value) throws SQLException {
        if (value == null) {
            statement.setObject(index, null);
        } else {
            statement.setDouble(index, value);
        }
    }

    private record SqlAndParams(String sql, List<Object> params) {
        void bind(PreparedStatement statement) throws SQLException {
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }
        }
    }
}
