package com.xiaoji.toolkit.iam.service;

import com.xiaoji.toolkit.iam.model.IamSession;
import com.xiaoji.toolkit.iam.model.LoginResult;
import com.xiaoji.toolkit.shared.constants.ResultCode;
import com.xiaoji.toolkit.shared.exception.BizException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryIamAuthService implements IamAuthService {

    private static final long TOKEN_TTL_MILLIS = 12L * 60L * 60L * 1000L;

    private final Map<String, AccountRecord> accountByUsername = new HashMap<String, AccountRecord>();
    private final ConcurrentHashMap<String, IamSession> sessionByToken = new ConcurrentHashMap<String, IamSession>();

    @PostConstruct
    public void init() {
        addAccount(new AccountRecord(
                1L,
                "admin",
                "admin123",
                "Administrator",
                setOf("ADMIN"),
                setOf("IAM_ADMIN", "RADAR_MANAGE", "RULE_MANAGE", "NOTIFY_MANAGE")
        ));

        addAccount(new AccountRecord(
                2L,
                "user",
                "user123",
                "Toolkit User",
                setOf("USER"),
                setOf("RADAR_VIEW", "HABIT_USE")
        ));
    }

    @Override
    public LoginResult login(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "username or password is empty");
        }

        AccountRecord account = accountByUsername.get(username);
        if (account == null || !account.password.equals(password)) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "invalid username or password");
        }

        long expireAt = System.currentTimeMillis() + TOKEN_TTL_MILLIS;
        String token = UUID.randomUUID().toString().replace("-", "");
        IamSession session = new IamSession(
                account.userId,
                account.username,
                account.displayName,
                account.roles,
                account.permissions,
                expireAt
        );
        sessionByToken.put(token, session);

        return new LoginResult(token, expireAt);
    }

    @Override
    public IamSession getSessionByToken(String token) {
        if (isBlank(token)) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "missing access token");
        }

        IamSession session = sessionByToken.get(token);
        if (session == null) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "invalid access token");
        }

        if (session.getExpireAt() < System.currentTimeMillis()) {
            sessionByToken.remove(token);
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "access token expired");
        }

        return session;
    }

    @Override
    public void requirePermission(String token, String permission) {
        IamSession session = getSessionByToken(token);
        if (!session.getPermissions().contains(permission)) {
            throw new BizException(ResultCode.FORBIDDEN.getCode(), "permission denied: " + permission);
        }
    }

    private void addAccount(AccountRecord account) {
        accountByUsername.put(account.username, account);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static Set<String> setOf(String... values) {
        return new HashSet<String>(Arrays.asList(values));
    }

    private static final class AccountRecord {
        private final Long userId;
        private final String username;
        private final String password;
        private final String displayName;
        private final Set<String> roles;
        private final Set<String> permissions;

        private AccountRecord(Long userId,
                              String username,
                              String password,
                              String displayName,
                              Set<String> roles,
                              Set<String> permissions) {
            this.userId = userId;
            this.username = username;
            this.password = password;
            this.displayName = displayName;
            this.roles = roles;
            this.permissions = permissions;
        }
    }
}
