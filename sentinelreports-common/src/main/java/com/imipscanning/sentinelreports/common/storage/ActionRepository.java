package com.imipscanning.sentinelreports.common.storage;

import com.imipscanning.sentinelreports.common.model.ReportAction;
import com.imipscanning.sentinelreports.common.model.StaffReportStats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ActionRepository {
    private final DatabaseManager databaseManager;

    public ActionRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public ReportAction add(ReportAction action) {
        String sql = """
                INSERT INTO report_actions(report_id, action_type, actor_uuid, actor_name, old_value, new_value, created_at)
                VALUES(?,?,?,?,?,?,?)
                """;
        try (Connection connection = databaseManager.connection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, action.reportId());
            statement.setString(2, action.actionType());
            statement.setString(3, JdbcMapper.uuid(action.actorUuid()));
            statement.setString(4, action.actorName());
            statement.setString(5, action.oldValue());
            statement.setString(6, action.newValue());
            statement.setLong(7, action.createdAt());
            statement.executeUpdate();
            try (var keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new ReportAction(keys.getLong(1), action.reportId(), action.actionType(), action.actorUuid(),
                            action.actorName(), action.oldValue(), action.newValue(), action.createdAt());
                }
            }
            return action;
        } catch (Exception ex) {
            throw new StorageException("Could not add report action", ex);
        }
    }

    public void add(long reportId, String type, UUID actorUuid, String actorName, String oldValue, String newValue) {
        add(new ReportAction(0L, reportId, type, actorUuid, actorName, oldValue, newValue, System.currentTimeMillis()));
    }

    public List<ReportAction> list(long reportId) {
        try (Connection connection = databaseManager.connection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM report_actions WHERE report_id = ? ORDER BY created_at ASC")) {
            statement.setLong(1, reportId);
            try (var rs = statement.executeQuery()) {
                List<ReportAction> actions = new ArrayList<>();
                while (rs.next()) {
                    actions.add(JdbcMapper.action(rs));
                }
                return actions;
            }
        } catch (Exception ex) {
            throw new StorageException("Could not list report actions", ex);
        }
    }

    public StaffReportStats staffStats(UUID staffUuid, String staffName) {
        try (Connection connection = databaseManager.connection()) {
            long claimed = count(connection, staffUuid, "CLAIM");
            long resolved = count(connection, staffUuid, "CLOSE_RESOLVED");
            long rejected = count(connection, staffUuid, "CLOSE_REJECTED");
            long falseReports = count(connection, staffUuid, "CLOSE_FALSE_REPORT");
            long notes = count(connection, staffUuid, "NOTE_ADD");
            long evidence = count(connection, staffUuid, "EVIDENCE_ADD");
            return new StaffReportStats(staffUuid, staffName, claimed, resolved, rejected, falseReports, notes, evidence);
        } catch (Exception ex) {
            throw new StorageException("Could not load staff stats", ex);
        }
    }

    private long count(Connection connection, UUID staffUuid, String type) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM report_actions WHERE actor_uuid = ? AND action_type = ?")) {
            statement.setString(1, staffUuid.toString());
            statement.setString(2, type);
            try (var rs = statement.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        }
    }
}
