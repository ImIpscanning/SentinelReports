package com.imipscanning.sentinelreports.common.storage;

import com.imipscanning.sentinelreports.common.model.ReportEvidence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class EvidenceRepository {
    private final DatabaseManager databaseManager;

    public EvidenceRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public ReportEvidence add(ReportEvidence evidence) {
        String sql = """
                INSERT INTO report_evidence(report_id, evidence_type, content, added_by_uuid, added_by_name, created_at)
                VALUES(?,?,?,?,?,?)
                """;
        try (Connection connection = databaseManager.connection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, evidence.reportId());
            statement.setString(2, evidence.evidenceType().name());
            statement.setString(3, evidence.content());
            statement.setString(4, JdbcMapper.uuid(evidence.addedByUuid()));
            statement.setString(5, evidence.addedByName());
            statement.setLong(6, evidence.createdAt());
            statement.executeUpdate();
            try (var keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new ReportEvidence(keys.getLong(1), evidence.reportId(), evidence.evidenceType(), evidence.content(),
                            evidence.addedByUuid(), evidence.addedByName(), evidence.createdAt());
                }
            }
            return evidence;
        } catch (Exception ex) {
            throw new StorageException("Could not add evidence", ex);
        }
    }

    public List<ReportEvidence> list(long reportId) {
        try (Connection connection = databaseManager.connection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM report_evidence WHERE report_id = ? ORDER BY created_at ASC")) {
            statement.setLong(1, reportId);
            try (var rs = statement.executeQuery()) {
                List<ReportEvidence> evidence = new ArrayList<>();
                while (rs.next()) {
                    evidence.add(JdbcMapper.evidence(rs));
                }
                return evidence;
            }
        } catch (Exception ex) {
            throw new StorageException("Could not list evidence", ex);
        }
    }

    public boolean remove(long reportId, long evidenceId) {
        try (Connection connection = databaseManager.connection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM report_evidence WHERE report_id = ? AND id = ?")) {
            statement.setLong(1, reportId);
            statement.setLong(2, evidenceId);
            return statement.executeUpdate() > 0;
        } catch (Exception ex) {
            throw new StorageException("Could not remove evidence", ex);
        }
    }
}
