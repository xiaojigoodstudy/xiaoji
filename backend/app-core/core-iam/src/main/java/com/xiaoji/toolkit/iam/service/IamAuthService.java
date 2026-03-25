package com.xiaoji.toolkit.iam.service;

import com.xiaoji.toolkit.iam.model.IamSession;
import com.xiaoji.toolkit.iam.model.LoginResult;

public interface IamAuthService {
    LoginResult login(String username, String password);

    IamSession getSessionByToken(String token);

    void requirePermission(String token, String permission);
}
