package com.imipscanning.sentinelreports.common.storage;

import com.imipscanning.sentinelreports.common.model.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

final class JdbcMapper {
    private JdbcMapper() {
    }

    static Report report(ResultSet rs) throws SQLException {
        return new Report(
                rs.getLong("id"),
                uuid(rs.getString("report_uuid")),
                uuid(rs.getString("reporter_uuid")),
                rs.getString("reporter_name"),
                uuid(rs.getString("target_uuid")),
                rs.getString("target_name"),
                rs.getString("category"),
                rs.getString("reason"),
                ReportStatus.parse(rs.getString("status")).orElse(ReportStatus.OPEN),
                ReportPriority.parse(rs.getString("priority")).orElse(ReportPriority.MEDIUM),
                rs.getString("server_name"),
                rs.getString("world"),
                nullableDouble(rs, "x"),
                nullableDouble(rs, "y"),
                nullableDouble(rs, "z"),
                uuid(rs.getString("assigned_to_uuid")),
                rs.getString("assigned_to_name"),
                rs.getLong("assigned_at"),
                rs.getLong("created_at"),
                rs.getLong("updated_at"),
                nullableLong(rs, "closed_at"),
                uuid(rs.getString("closed_by_uuid")),
                rs.getString("closed_by_name"),
                rs.getString("close_reason")
        );
    }

    static ReportEvidence evidence(ResultSet rs) throws SQLException {
        return new ReportEvidence(
                rs.getLong("id"),
                rs.getLong("report_id"),
                EvidenceType.valueOf(rs.getString("evidence_type")),
                rs.getString("content"),
                uuid(rs.getString("added_by_uuid")),
                rs.getString("added_by_name"),
                rs.getLong("created_at")
        );
    }

    static ReportNote note(ResultSet rs) throws SQLException {
        return new ReportNote(
                rs.getLong("id"),
                rs.getLong("report_id"),
                rs.getString("note"),
                uuid(rs.getString("added_by_uuid")),
                rs.getString("added_by_name"),
                rs.getLong("created_at")
        );
    }

    static ReportAction action(ResultSet rs) throws SQLException {
        return new ReportAction(
                rs.getLong("id"),
                rs.getLong("report_id"),
                rs.getString("action_type"),
                uuid(rs.getString("actor_uuid")),
                rs.getString("actor_name"),
                rs.getString("old_value"),
                rs.getString("new_value"),
                rs.getLong("created_at")
        );
    }

    static UUID uuid(String value) {
        return value == null || value.isBlank() ? null : UUID.fromString(value);
    }

    static String uuid(UUID uuid) {
        return uuid == null ? null : uuid.toString();
    }

    private static Long nullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static Double nullableDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }
}
