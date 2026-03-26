USE xiaoji;

INSERT INTO iam_user (id, username, password_hash, display_name, status, created_at, updated_at, is_deleted)
VALUES
  (1, 'admin', 'admin123', 'Administrator', 1, NOW(3), NOW(3), 0),
  (2, 'user', 'user123', 'Toolkit User', 1, NOW(3), NOW(3), 0)
ON DUPLICATE KEY UPDATE updated_at = VALUES(updated_at);

INSERT INTO iam_role (id, role_code, role_name, created_at, updated_at, is_deleted)
VALUES
  (11, 'ADMIN', 'Administrator', NOW(3), NOW(3), 0),
  (12, 'USER', 'Normal User', NOW(3), NOW(3), 0)
ON DUPLICATE KEY UPDATE updated_at = VALUES(updated_at);

INSERT INTO iam_permission (id, perm_code, perm_name, created_at, updated_at, is_deleted)
VALUES
  (101, 'IAM_ADMIN', 'IAM Admin', NOW(3), NOW(3), 0),
  (102, 'RADAR_MANAGE', 'Radar Manage', NOW(3), NOW(3), 0),
  (103, 'RULE_MANAGE', 'Rule Manage', NOW(3), NOW(3), 0),
  (104, 'NOTIFY_MANAGE', 'Notify Manage', NOW(3), NOW(3), 0),
  (105, 'RADAR_VIEW', 'Radar View', NOW(3), NOW(3), 0),
  (106, 'HABIT_USE', 'Habit Use', NOW(3), NOW(3), 0)
ON DUPLICATE KEY UPDATE updated_at = VALUES(updated_at);

INSERT INTO iam_user_role (id, user_id, role_id, created_at)
VALUES
  (1001, 1, 11, NOW(3)),
  (1002, 2, 12, NOW(3))
ON DUPLICATE KEY UPDATE created_at = VALUES(created_at);

INSERT INTO iam_role_permission (id, role_id, permission_id, created_at)
VALUES
  (2001, 11, 101, NOW(3)),
  (2002, 11, 102, NOW(3)),
  (2003, 11, 103, NOW(3)),
  (2004, 11, 104, NOW(3)),
  (2005, 12, 105, NOW(3)),
  (2006, 12, 106, NOW(3))
ON DUPLICATE KEY UPDATE created_at = VALUES(created_at);
