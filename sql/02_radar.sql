USE daily_toolkit;

CREATE TABLE IF NOT EXISTS radar_source (
  id BIGINT PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  source_type VARCHAR(64) NOT NULL,
  source_url VARCHAR(1024) NOT NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  last_fetch_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_radar_source_enabled (enabled),
  KEY idx_radar_source_type (source_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS radar_task (
  id BIGINT PRIMARY KEY,
  source_id BIGINT NOT NULL,
  task_name VARCHAR(128) NOT NULL,
  cron_expression VARCHAR(128) NOT NULL,
  status VARCHAR(32) NOT NULL,
  last_run_at DATETIME(3) NULL,
  run_count BIGINT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_radar_task_source_id (source_id),
  KEY idx_radar_task_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS radar_item (
  id BIGINT PRIMARY KEY,
  source_id BIGINT NOT NULL,
  title VARCHAR(512) NOT NULL,
  content MEDIUMTEXT NULL,
  source_url VARCHAR(1024) NULL,
  source_type VARCHAR(64) NULL,
  tags_json TEXT NULL,
  published_at DATETIME(3) NULL,
  discovered_at DATETIME(3) NOT NULL,
  dedup_hash VARCHAR(128) NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_radar_item_dedup_hash (dedup_hash),
  KEY idx_radar_item_source_id (source_id),
  KEY idx_radar_item_discovered_at (discovered_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
