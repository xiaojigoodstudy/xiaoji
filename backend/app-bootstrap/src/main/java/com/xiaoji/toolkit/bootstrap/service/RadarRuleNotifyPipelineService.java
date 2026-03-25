package com.xiaoji.toolkit.bootstrap.service;

import com.xiaoji.toolkit.bootstrap.api.RadarRuleNotifyResponse;
import com.xiaoji.toolkit.notification.model.NotificationLog;
import com.xiaoji.toolkit.notification.model.SendNotificationCommand;
import com.xiaoji.toolkit.notification.service.NotificationService;
import com.xiaoji.toolkit.ruleengine.model.RuleDefinition;
import com.xiaoji.toolkit.ruleengine.model.RuleEvaluateResult;
import com.xiaoji.toolkit.ruleengine.service.RuleEngineService;
import com.xiaoji.toolkit.shared.constants.ResultCode;
import com.xiaoji.toolkit.shared.exception.BizException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class RadarRuleNotifyPipelineService {

    private final RuleEngineService ruleEngineService;
    private final NotificationService notificationService;

    public RadarRuleNotifyPipelineService(RuleEngineService ruleEngineService, NotificationService notificationService) {
        this.ruleEngineService = ruleEngineService;
        this.notificationService = notificationService;
    }

    public RadarRuleNotifyResponse process(String content, String sourceType, String target) {
        if (isBlank(content) || isBlank(target)) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "content and target are required");
        }

        RuleEvaluateResult evaluateResult = ruleEngineService.evaluate(content, sourceType);
        List<RuleDefinition> matchedRules = evaluateResult.getMatchedRules();

        List<Long> matchedRuleIds = new ArrayList<Long>();
        LinkedHashSet<String> channelSet = new LinkedHashSet<String>();
        for (RuleDefinition rule : matchedRules) {
            matchedRuleIds.add(rule.getId());
            channelSet.add(rule.getNotifyChannel());
        }

        List<String> channels = new ArrayList<String>(channelSet);
        List<NotificationLog> logs = new ArrayList<NotificationLog>();

        if (!channels.isEmpty()) {
            SendNotificationCommand command = new SendNotificationCommand(
                    "Radar Match Alert",
                    content,
                    target,
                    channels
            );
            logs = notificationService.send(command);
        }

        return new RadarRuleNotifyResponse(matchedRules.size(), matchedRuleIds, channels, logs);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
