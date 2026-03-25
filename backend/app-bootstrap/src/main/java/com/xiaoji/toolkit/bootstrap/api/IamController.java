package com.xiaoji.toolkit.bootstrap.api;

import com.xiaoji.toolkit.iam.model.IamSession;
import com.xiaoji.toolkit.iam.model.LoginResult;
import com.xiaoji.toolkit.iam.service.IamAuthService;
import com.xiaoji.toolkit.shared.constants.ResultCode;
import com.xiaoji.toolkit.shared.exception.BizException;
import com.xiaoji.toolkit.shared.model.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/iam")
public class IamController {

    private final IamAuthService iamAuthService;

    public IamController(IamAuthService iamAuthService) {
        this.iamAuthService = iamAuthService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResult> login(@RequestBody LoginRequest request) {
        LoginResult result = iamAuthService.login(request.getUsername(), request.getPassword());
        return ApiResponse.success(result);
    }

    @GetMapping("/me")
    public ApiResponse<IamSession> me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        IamSession session = iamAuthService.getSessionByToken(extractToken(authorization));
        return ApiResponse.success(session);
    }

    @GetMapping("/admin/ping")
    public ApiResponse<Map<String, Object>> adminPing(@RequestHeader(value = "Authorization", required = false) String authorization) {
        iamAuthService.requirePermission(extractToken(authorization), "IAM_ADMIN");

        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("ok", Boolean.TRUE);
        payload.put("scope", "IAM_ADMIN");
        payload.put("timestamp", System.currentTimeMillis());
        return ApiResponse.success(payload);
    }

    private String extractToken(String authorization) {
        if (authorization == null || authorization.trim().isEmpty()) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "missing authorization header");
        }

        String prefix = "Bearer ";
        if (!authorization.startsWith(prefix)) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "invalid authorization header");
        }

        String token = authorization.substring(prefix.length()).trim();
        if (token.isEmpty()) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "missing access token");
        }
        return token;
    }
}
