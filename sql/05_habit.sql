USE daily_toolkit;

CREATE TABLE IF NOT EXISTS habit_item (
  id BIGINT PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_habit_item_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS habit_checkin_record (
  id BIGINT PRIMARY KEY,
  habit_item_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  checkin_date DATE NOT NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_habit_checkin_unique (habit_item_id, user_id, checkin_date),
  KEY idx_habit_checkin_user_id (user_id),
  KEY idx_habit_checkin_date (checkin_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
