package com.xiaoji.toolkit.bootstrap.api;

import com.xiaoji.toolkit.iam.service.IamAuthService;
import com.xiaoji.toolkit.ruleengine.model.RuleDefinition;
import com.xiaoji.toolkit.ruleengine.model.RuleEvaluateResult;
import com.xiaoji.toolkit.ruleengine.service.RuleEngineService;
import com.xiaoji.toolkit.shared.constants.ResultCode;
import com.xiaoji.toolkit.shared.exception.BizException;
import com.xiaoji.toolkit.shared.model.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

    private final RuleEngineService ruleEngineService;
    private final IamAuthService iamAuthService;

    public RuleController(RuleEngineService ruleEngineService, IamAuthService iamAuthService) {
        this.ruleEngineService = ruleEngineService;
        this.iamAuthService = iamAuthService;
    }

    @PostMapping
    public ApiResponse<RuleDefinition> createRule(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                  @RequestBody CreateRuleRequest request) {
        iamAuthService.requirePermission(extractToken(authorization), "RULE_MANAGE");
        boolean enabled = request.getEnabled() == null || request.getEnabled();
        RuleDefinition rule = ruleEngineService.createRule(
                request.getRuleName(),
                request.getKeyword(),
                request.getSourceType(),
                request.getNotifyChannel(),
                enabled
        );
        return ApiResponse.success(rule);
    }

    @GetMapping
    public ApiResponse<List<RuleDefinition>> listRules(@RequestHeader(value = "Authorization", required = false) String authorization) {
        iamAuthService.getSessionByToken(extractToken(authorization));
        return ApiResponse.success(ruleEngineService.listRules());
    }

    @PostMapping("/{ruleId}/enabled")
    public ApiResponse<RuleDefinition> setRuleEnabled(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                      @PathVariable("ruleId") Long ruleId,
                                                      @RequestParam("enabled") boolean enabled) {
        iamAuthService.requirePermission(extractToken(authorization), "RULE_MANAGE");
        return ApiResponse.success(ruleEngineService.setRuleEnabled(ruleId, enabled));
    }

    @PostMapping("/evaluate")
    public ApiResponse<RuleEvaluateResult> evaluate(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                    @RequestBody RuleEvaluateRequest request) {
        iamAuthService.requirePermission(extractToken(authorization), "RULE_MANAGE");
        return ApiResponse.success(ruleEngineService.evaluate(request.getContent(), request.getSourceType()));
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
