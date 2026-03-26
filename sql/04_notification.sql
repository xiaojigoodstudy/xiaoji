USE xiaoji;

CREATE TABLE IF NOT EXISTS notification_task (
  id BIGINT PRIMARY KEY,
  title VARCHAR(256) NOT NULL,
  content TEXT NOT NULL,
  target VARCHAR(128) NOT NULL,
  channels_json TEXT NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  KEY idx_notification_task_status (status),
  KEY idx_notification_task_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS notification_log (
  id BIGINT PRIMARY KEY,
  task_id BIGINT NULL,
  channel VARCHAR(64) NOT NULL,
  target VARCHAR(128) NOT NULL,
  title VARCHAR(256) NOT NULL,
  content TEXT NOT NULL,
  status VARCHAR(32) NOT NULL,
  detail VARCHAR(512) NULL,
  created_at DATETIME(3) NOT NULL,
  KEY idx_notification_log_task_id (task_id),
  KEY idx_notification_log_channel (channel),
  KEY idx_notification_log_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
