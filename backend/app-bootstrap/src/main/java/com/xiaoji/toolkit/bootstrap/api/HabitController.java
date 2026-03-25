package com.xiaoji.toolkit.bootstrap.api;

import com.xiaoji.toolkit.habit.model.HabitCheckinRecord;
import com.xiaoji.toolkit.habit.model.HabitItem;
import com.xiaoji.toolkit.habit.model.HabitStats;
import com.xiaoji.toolkit.habit.service.HabitService;
import com.xiaoji.toolkit.iam.model.IamSession;
import com.xiaoji.toolkit.iam.service.IamAuthService;
import com.xiaoji.toolkit.shared.constants.ResultCode;
import com.xiaoji.toolkit.shared.exception.BizException;
import com.xiaoji.toolkit.shared.model.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/habits")
public class HabitController {

    private final HabitService habitService;
    private final IamAuthService iamAuthService;

    public HabitController(HabitService habitService, IamAuthService iamAuthService) {
        this.habitService = habitService;
        this.iamAuthService = iamAuthService;
    }

    @PostMapping
    public ApiResponse<HabitItem> createItem(@RequestHeader(value = "Authorization", required = false) String authorization,
                                             @RequestBody CreateHabitItemRequest request) {
        iamAuthService.getSessionByToken(extractToken(authorization));
        boolean enabled = request.getEnabled() == null || request.getEnabled();
        return ApiResponse.success(habitService.createHabitItem(request.getName(), enabled));
    }

    @GetMapping
    public ApiResponse<List<HabitItem>> listItems(@RequestHeader(value = "Authorization", required = false) String authorization) {
        iamAuthService.getSessionByToken(extractToken(authorization));
        return ApiResponse.success(habitService.listHabitItems());
    }

    @PostMapping("/{habitItemId}/checkin")
    public ApiResponse<HabitCheckinRecord> checkin(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                   @PathVariable("habitItemId") Long habitItemId,
                                                   @RequestBody(required = false) HabitCheckinRequest request) {
        IamSession session = iamAuthService.getSessionByToken(extractToken(authorization));
        String checkinDate = request == null ? null : request.getCheckinDate();
        return ApiResponse.success(habitService.checkin(habitItemId, session.getUserId(), checkinDate));
    }

    @GetMapping("/records")
    public ApiResponse<List<HabitCheckinRecord>> listRecords(@RequestHeader(value = "Authorization", required = false) String authorization) {
        iamAuthService.getSessionByToken(extractToken(authorization));
        return ApiResponse.success(habitService.listCheckinRecords());
    }

    @GetMapping("/stats")
    public ApiResponse<HabitStats> stats(@RequestHeader(value = "Authorization", required = false) String authorization) {
        iamAuthService.getSessionByToken(extractToken(authorization));
        return ApiResponse.success(habitService.stats());
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
