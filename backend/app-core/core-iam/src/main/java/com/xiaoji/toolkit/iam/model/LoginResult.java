package com.xiaoji.toolkit.iam.model;

public class LoginResult {
    private final String accessToken;
    private final String tokenType;
    private final long expireAt;

    public LoginResult(String accessToken, long expireAt) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.expireAt = expireAt;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpireAt() {
        return expireAt;
    }
}
