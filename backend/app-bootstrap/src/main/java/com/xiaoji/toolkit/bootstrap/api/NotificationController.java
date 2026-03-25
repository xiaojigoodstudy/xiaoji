package com.xiaoji.toolkit.bootstrap.api;

import com.xiaoji.toolkit.iam.service.IamAuthService;
import com.xiaoji.toolkit.notification.model.NotificationLog;
import com.xiaoji.toolkit.notification.model.SendNotificationCommand;
import com.xiaoji.toolkit.notification.service.NotificationService;
import com.xiaoji.toolkit.shared.constants.ResultCode;
import com.xiaoji.toolkit.shared.exception.BizException;
import com.xiaoji.toolkit.shared.model.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final IamAuthService iamAuthService;

    public NotificationController(NotificationService notificationService, IamAuthService iamAuthService) {
        this.notificationService = notificationService;
        this.iamAuthService = iamAuthService;
    }

    @PostMapping("/send")
    public ApiResponse<List<NotificationLog>> send(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                    @RequestBody SendNotificationRequest request) {
        iamAuthService.requirePermission(extractToken(authorization), "NOTIFY_MANAGE");

        SendNotificationCommand command = new SendNotificationCommand(
                request.getTitle(),
                request.getContent(),
                request.getTarget(),
                request.getChannels()
        );
        return ApiResponse.success(notificationService.send(command));
    }

    @GetMapping("/logs")
    public ApiResponse<List<NotificationLog>> logs(@RequestHeader(value = "Authorization", required = false) String authorization) {
        iamAuthService.requirePermission(extractToken(authorization), "NOTIFY_MANAGE");
        return ApiResponse.success(notificationService.listLogs());
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
