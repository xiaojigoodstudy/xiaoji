CREATE TABLE IF NOT EXISTS iam_user (
  id BIGINT PRIMARY KEY,
  username VARCHAR(64) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  display_name VARCHAR(128) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_iam_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS iam_role (
  id BIGINT PRIMARY KEY,
  role_code VARCHAR(64) NOT NULL,
  role_name VARCHAR(128) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_iam_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS iam_permission (
  id BIGINT PRIMARY KEY,
  perm_code VARCHAR(64) NOT NULL,
  perm_name VARCHAR(128) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_iam_perm_code (perm_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS iam_user_role (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_iam_user_role (user_id, role_id),
  KEY idx_iam_user_role_user_id (user_id),
  KEY idx_iam_user_role_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS iam_role_permission (
  id BIGINT PRIMARY KEY,
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_iam_role_perm (role_id, permission_id),
  KEY idx_iam_role_perm_role_id (role_id),
  KEY idx_iam_role_perm_perm_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS iam_token_session (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  access_token VARCHAR(128) NOT NULL,
  expire_at DATETIME(3) NOT NULL,
  last_seen_at DATETIME(3) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  UNIQUE KEY uk_iam_token_session_token (access_token),
  KEY idx_iam_token_session_user_id (user_id),
  KEY idx_iam_token_session_expire_at (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
