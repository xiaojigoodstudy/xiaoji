package com.xiaoji.toolkit.iam.service;

import com.xiaoji.toolkit.iam.model.IamSession;
import com.xiaoji.toolkit.iam.model.LoginResult;
import com.xiaoji.toolkit.shared.constants.ResultCode;
import com.xiaoji.toolkit.shared.exception.BizException;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Profile("mysql")
public class MysqlIamAuthService implements IamAuthService {

    private static final long TOKEN_TTL_MILLIS = 12L * 60L * 60L * 1000L;

    private static final String SQL_FIND_ACCOUNT =
            "SELECT id, username, password_hash, display_name " +
                    "FROM iam_user " +
                    "WHERE username = ? AND status = 1 AND is_deleted = 0 " +
                    "LIMIT 1";

    private static final String SQL_INSERT_TOKEN_SESSION =
            "INSERT INTO iam_token_session " +
                    "(id, user_id, access_token, expire_at, last_seen_at, created_at, updated_at, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, 1)";

    private static final String SQL_FIND_SESSION =
            "SELECT s.user_id, u.username, u.display_name, s.expire_at " +
                    "FROM iam_token_session s " +
                    "INNER JOIN iam_user u ON u.id = s.user_id " +
                    "WHERE s.access_token = ? AND s.status = 1 AND u.status = 1 AND u.is_deleted = 0 " +
                    "LIMIT 1";

    private static final String SQL_TOUCH_SESSION =
            "UPDATE iam_token_session SET last_seen_at = ?, updated_at = ? " +
                    "WHERE access_token = ? AND status = 1";

    private static final String SQL_EXPIRE_SESSION =
            "UPDATE iam_token_session SET status = 0, updated_at = ? " +
                    "WHERE access_token = ? AND status = 1";

    private static final String SQL_FIND_ROLES =
            "SELECT DISTINCT r.role_code " +
                    "FROM iam_role r " +
                    "INNER JOIN iam_user_role ur ON ur.role_id = r.id " +
                    "WHERE ur.user_id = ? AND r.is_deleted = 0";

    private static final String SQL_FIND_PERMISSIONS =
            "SELECT DISTINCT p.perm_code " +
                    "FROM iam_permission p " +
                    "INNER JOIN iam_role_permission rp ON rp.permission_id = p.id " +
                    "INNER JOIN iam_user_role ur ON ur.role_id = rp.role_id " +
                    "WHERE ur.user_id = ? AND p.is_deleted = 0";

    private final JdbcTemplate jdbcTemplate;
    private final AtomicLong sessionIdGenerator = new AtomicLong(System.currentTimeMillis() * 1000L);

    public MysqlIamAuthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public LoginResult login(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "username or password is empty");
        }

        AccountRecord account = findAccountByUsername(username);
        if (account == null || !password.equals(account.passwordHash)) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "invalid username or password");
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        long expireAt = System.currentTimeMillis() + TOKEN_TTL_MILLIS;
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Timestamp expireAtTs = new Timestamp(expireAt);

        jdbcTemplate.update(
                SQL_INSERT_TOKEN_SESSION,
                nextSessionId(),
                account.userId,
                token,
                expireAtTs,
                now,
                now,
                now
        );

        return new LoginResult(token, expireAt);
    }

    @Override
    public IamSession getSessionByToken(String token) {
        if (isBlank(token)) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "missing access token");
        }

        SessionRecord session = findSessionByToken(token);
        if (session == null) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "invalid access token");
        }

        long expireAt = session.expireAt.getTime();
        long now = System.currentTimeMillis();
        if (expireAt < now) {
            jdbcTemplate.update(SQL_EXPIRE_SESSION, new Timestamp(now), token);
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "access token expired");
        }

        Timestamp touchAt = new Timestamp(now);
        jdbcTemplate.update(SQL_TOUCH_SESSION, touchAt, touchAt, token);

        return new IamSession(
                session.userId,
                session.username,
                session.displayName,
                findRoleCodes(session.userId),
                findPermissionCodes(session.userId),
                expireAt
        );
    }

    @Override
    public void requirePermission(String token, String permission) {
        IamSession session = getSessionByToken(token);
        if (!session.getPermissions().contains(permission)) {
            throw new BizException(ResultCode.FORBIDDEN.getCode(), "permission denied: " + permission);
        }
    }

    private AccountRecord findAccountByUsername(String username) {
        try {
            return jdbcTemplate.queryForObject(SQL_FIND_ACCOUNT, new Object[]{username}, this::mapAccount);
        } catch (EmptyResultDataAccessException ignore) {
            return null;
        }
    }

    private SessionRecord findSessionByToken(String token) {
        try {
            return jdbcTemplate.queryForObject(SQL_FIND_SESSION, new Object[]{token}, this::mapSession);
        } catch (EmptyResultDataAccessException ignore) {
            return null;
        }
    }

    private Set<String> findRoleCodes(Long userId) {
        List<String> roles = jdbcTemplate.queryForList(SQL_FIND_ROLES, new Object[]{userId}, String.class);
        return new HashSet<String>(roles);
    }

    private Set<String> findPermissionCodes(Long userId) {
        List<String> permissions = jdbcTemplate.queryForList(SQL_FIND_PERMISSIONS, new Object[]{userId}, String.class);
        return new HashSet<String>(permissions);
    }

    private AccountRecord mapAccount(ResultSet rs, int rowNum) throws SQLException {
        return new AccountRecord(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("display_name")
        );
    }

    private SessionRecord mapSession(ResultSet rs, int rowNum) throws SQLException {
        return new SessionRecord(
                rs.getLong("user_id"),
                rs.getString("username"),
                rs.getString("display_name"),
                rs.getTimestamp("expire_at")
        );
    }

    private long nextSessionId() {
        return sessionIdGenerator.incrementAndGet();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static final class AccountRecord {
        private final Long userId;
        private final String username;
        private final String passwordHash;
        private final String displayName;

        private AccountRecord(Long userId, String username, String passwordHash, String displayName) {
            this.userId = userId;
            this.username = username;
            this.passwordHash = passwordHash;
            this.displayName = displayName;
        }
    }

    private static final class SessionRecord {
        private final Long userId;
        private final String username;
        private final String displayName;
        private final Timestamp expireAt;

        private SessionRecord(Long userId, String username, String displayName, Timestamp expireAt) {
            this.userId = userId;
            this.username = username;
            this.displayName = displayName;
            this.expireAt = expireAt;
        }
    }
}
