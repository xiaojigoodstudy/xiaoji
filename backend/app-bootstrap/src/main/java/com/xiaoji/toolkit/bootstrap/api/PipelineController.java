package com.xiaoji.toolkit.bootstrap.api;

import com.xiaoji.toolkit.bootstrap.service.RadarRuleNotifyPipelineService;
import com.xiaoji.toolkit.iam.service.IamAuthService;
import com.xiaoji.toolkit.shared.constants.ResultCode;
import com.xiaoji.toolkit.shared.exception.BizException;
import com.xiaoji.toolkit.shared.model.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pipeline")
public class PipelineController {

    private final RadarRuleNotifyPipelineService pipelineService;
    private final IamAuthService iamAuthService;

    public PipelineController(RadarRuleNotifyPipelineService pipelineService, IamAuthService iamAuthService) {
        this.pipelineService = pipelineService;
        this.iamAuthService = iamAuthService;
    }

    @PostMapping("/radar-rule-notify")
    public ApiResponse<RadarRuleNotifyResponse> radarRuleNotify(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                                @RequestBody RadarRuleNotifyRequest request) {
        iamAuthService.requirePermission(extractToken(authorization), "RADAR_MANAGE");
        RadarRuleNotifyResponse response = pipelineService.process(
                request.getContent(),
                request.getSourceType(),
                request.getTarget()
        );
        return ApiResponse.success(response);
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
