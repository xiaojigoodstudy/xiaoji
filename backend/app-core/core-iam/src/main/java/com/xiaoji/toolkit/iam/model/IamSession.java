package com.xiaoji.toolkit.iam.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class IamSession {
    private final Long userId;
    private final String username;
    private final String displayName;
    private final Set<String> roles;
    private final Set<String> permissions;
    private final long expireAt;

    public IamSession(Long userId,
                      String username,
                      String displayName,
                      Set<String> roles,
                      Set<String> permissions,
                      long expireAt) {
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.roles = roles == null ? Collections.<String>emptySet() : Collections.unmodifiableSet(new HashSet<String>(roles));
        this.permissions = permissions == null ? Collections.<String>emptySet() : Collections.unmodifiableSet(new HashSet<String>(permissions));
        this.expireAt = expireAt;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public long getExpireAt() {
        return expireAt;
    }
}
