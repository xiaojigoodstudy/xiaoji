USE daily_toolkit;

CREATE TABLE IF NOT EXISTS rule_definition (
  id BIGINT PRIMARY KEY,
  rule_name VARCHAR(128) NOT NULL,
  keyword VARCHAR(256) NOT NULL,
  source_type VARCHAR(64) NULL,
  notify_channel VARCHAR(64) NOT NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_rule_definition_enabled (enabled),
  KEY idx_rule_definition_source_type (source_type),
  KEY idx_rule_definition_notify_channel (notify_channel)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS rule_evaluate_log (
  id BIGINT PRIMARY KEY,
  content TEXT NOT NULL,
  source_type VARCHAR(64) NULL,
  matched_rule_ids_json TEXT NULL,
  created_at DATETIME(3) NOT NULL,
  KEY idx_rule_eval_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
