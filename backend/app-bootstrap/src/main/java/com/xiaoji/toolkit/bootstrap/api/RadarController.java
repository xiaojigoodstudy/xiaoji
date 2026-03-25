package com.xiaoji.toolkit.bootstrap.api;

import com.xiaoji.toolkit.iam.service.IamAuthService;
import com.xiaoji.toolkit.radar.model.RadarSource;
import com.xiaoji.toolkit.radar.model.RadarTask;
import com.xiaoji.toolkit.radar.service.RadarService;
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
@RequestMapping("/api/radar")
public class RadarController {

    private final RadarService radarService;
    private final IamAuthService iamAuthService;

    public RadarController(RadarService radarService, IamAuthService iamAuthService) {
        this.radarService = radarService;
        this.iamAuthService = iamAuthService;
    }

    @PostMapping("/sources")
    public ApiResponse<RadarSource> createSource(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                  @RequestBody CreateRadarSourceRequest request) {
        iamAuthService.requirePermission(extractToken(authorization), "RADAR_MANAGE");
        boolean enabled = request.getEnabled() == null || request.getEnabled();
        RadarSource source = radarService.createSource(request.getName(), request.getSourceType(), request.getSourceUrl(), enabled);
        return ApiResponse.success(source);
    }

    @GetMapping("/sources")
    public ApiResponse<List<RadarSource>> listSources(@RequestHeader(value = "Authorization", required = false) String authorization) {
        iamAuthService.getSessionByToken(extractToken(authorization));
        return ApiResponse.success(radarService.listSources());
    }

    @PostMapping("/sources/{sourceId}/enabled")
    public ApiResponse<RadarSource> updateSourceEnabled(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                         @PathVariable("sourceId") Long sourceId,
                                                         @RequestParam("enabled") boolean enabled) {
        iamAuthService.requirePermission(extractToken(authorization), "RADAR_MANAGE");
        return ApiResponse.success(radarService.setSourceEnabled(sourceId, enabled));
    }

    @PostMapping("/tasks")
    public ApiResponse<RadarTask> createTask(@RequestHeader(value = "Authorization", required = false) String authorization,
                                             @RequestBody CreateRadarTaskRequest request) {
        iamAuthService.requirePermission(extractToken(authorization), "RADAR_MANAGE");
        return ApiResponse.success(radarService.createTask(request.getSourceId(), request.getTaskName(), request.getCronExpression()));
    }

    @GetMapping("/tasks")
    public ApiResponse<List<RadarTask>> listTasks(@RequestHeader(value = "Authorization", required = false) String authorization) {
        iamAuthService.getSessionByToken(extractToken(authorization));
        return ApiResponse.success(radarService.listTasks());
    }

    @PostMapping("/tasks/{taskId}/run")
    public ApiResponse<RadarTask> runTask(@RequestHeader(value = "Authorization", required = false) String authorization,
                                          @PathVariable("taskId") Long taskId) {
        iamAuthService.requirePermission(extractToken(authorization), "RADAR_MANAGE");
        return ApiResponse.success(radarService.runTask(taskId));
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
