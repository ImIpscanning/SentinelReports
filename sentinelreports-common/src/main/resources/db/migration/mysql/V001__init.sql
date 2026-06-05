CREATE TABLE IF NOT EXISTS reports (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  report_uuid VARCHAR(36) NOT NULL UNIQUE,
  reporter_uuid VARCHAR(36) NOT NULL,
  reporter_name VARCHAR(32) NOT NULL,
  target_uuid VARCHAR(36) NOT NULL,
  target_name VARCHAR(32) NOT NULL,
  category VARCHAR(64) NOT NULL,
  reason TEXT NOT NULL,
  status VARCHAR(32) NOT NULL,
  priority VARCHAR(32) NOT NULL,
  server_name VARCHAR(64) NOT NULL,
  world VARCHAR(128),
  x DOUBLE,
  y DOUBLE,
  z DOUBLE,
  assigned_to_uuid VARCHAR(36),
  assigned_to_name VARCHAR(32),
  assigned_at BIGINT NOT NULL DEFAULT 0,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL,
  closed_at BIGINT,
  closed_by_uuid VARCHAR(36),
  closed_by_name VARCHAR(32),
  close_reason TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS report_evidence (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  report_id BIGINT NOT NULL,
  evidence_type VARCHAR(32) NOT NULL,
  content TEXT NOT NULL,
  added_by_uuid VARCHAR(36) NOT NULL,
  added_by_name VARCHAR(32) NOT NULL,
  created_at BIGINT NOT NULL,
  CONSTRAINT fk_evidence_report FOREIGN KEY(report_id) REFERENCES reports(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS report_notes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  report_id BIGINT NOT NULL,
  note TEXT NOT NULL,
  added_by_uuid VARCHAR(36) NOT NULL,
  added_by_name VARCHAR(32) NOT NULL,
  created_at BIGINT NOT NULL,
  CONSTRAINT fk_notes_report FOREIGN KEY(report_id) REFERENCES reports(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS report_actions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  report_id BIGINT NOT NULL,
  action_type VARCHAR(64) NOT NULL,
  actor_uuid VARCHAR(36) NOT NULL,
  actor_name VARCHAR(32) NOT NULL,
  old_value TEXT,
  new_value TEXT,
  created_at BIGINT NOT NULL,
  CONSTRAINT fk_actions_report FOREIGN KEY(report_id) REFERENCES reports(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS report_categories (
  id VARCHAR(64) PRIMARY KEY,
  name TEXT NOT NULL,
  priority VARCHAR(32) NOT NULL,
  enabled BOOLEAN NOT NULL,
  created_at BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_reports_reporter_uuid ON reports(reporter_uuid);
CREATE INDEX idx_reports_target_uuid ON reports(target_uuid);
CREATE INDEX idx_reports_status ON reports(status);
CREATE INDEX idx_reports_priority ON reports(priority);
CREATE INDEX idx_reports_category ON reports(category);
CREATE INDEX idx_reports_server_name ON reports(server_name);
CREATE INDEX idx_reports_created_at ON reports(created_at);
CREATE INDEX idx_reports_assigned_to_uuid ON reports(assigned_to_uuid);
CREATE INDEX idx_report_evidence_report_id ON report_evidence(report_id);
CREATE INDEX idx_report_notes_report_id ON report_notes(report_id);
CREATE INDEX idx_report_actions_report_id ON report_actions(report_id);
