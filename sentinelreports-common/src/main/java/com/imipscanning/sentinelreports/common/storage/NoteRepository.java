package com.imipscanning.sentinelreports.common.storage;

import com.imipscanning.sentinelreports.common.model.ReportNote;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class NoteRepository {
    private final DatabaseManager databaseManager;

    public NoteRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public ReportNote add(ReportNote note) {
        String sql = "INSERT INTO report_notes(report_id, note, added_by_uuid, added_by_name, created_at) VALUES(?,?,?,?,?)";
        try (Connection connection = databaseManager.connection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, note.reportId());
            statement.setString(2, note.note());
            statement.setString(3, JdbcMapper.uuid(note.addedByUuid()));
            statement.setString(4, note.addedByName());
            statement.setLong(5, note.createdAt());
            statement.executeUpdate();
            try (var keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new ReportNote(keys.getLong(1), note.reportId(), note.note(), note.addedByUuid(), note.addedByName(), note.createdAt());
                }
            }
            return note;
        } catch (Exception ex) {
            throw new StorageException("Could not add note", ex);
        }
    }

    public List<ReportNote> list(long reportId) {
        try (Connection connection = databaseManager.connection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM report_notes WHERE report_id = ? ORDER BY created_at ASC")) {
            statement.setLong(1, reportId);
            try (var rs = statement.executeQuery()) {
                List<ReportNote> notes = new ArrayList<>();
                while (rs.next()) {
                    notes.add(JdbcMapper.note(rs));
                }
                return notes;
            }
        } catch (Exception ex) {
            throw new StorageException("Could not list notes", ex);
        }
    }
}
