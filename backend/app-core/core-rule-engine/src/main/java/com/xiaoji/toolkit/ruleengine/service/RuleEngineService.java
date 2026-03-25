package com.xiaoji.toolkit.ruleengine.service;

import com.xiaoji.toolkit.ruleengine.model.RuleDefinition;
import com.xiaoji.toolkit.ruleengine.model.RuleEvaluateResult;

import java.util.List;

public interface RuleEngineService {
    RuleDefinition createRule(String ruleName,
                              String keyword,
                              String sourceType,
                              String notifyChannel,
                              boolean enabled);

    List<RuleDefinition> listRules();

    RuleDefinition setRuleEnabled(Long ruleId, boolean enabled);

    RuleEvaluateResult evaluate(String content, String sourceType);
}
